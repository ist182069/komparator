package org.komparator.security.handler;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import org.komparator.security.CertUtil;
import org.komparator.security.CryptoUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;

public class EncryptionHandler implements SOAPHandler<SOAPMessageContext> {
	
	/** CryptoUtil class. */
	private CryptoUtil cryptoUtil = new CryptoUtil();
	
	/** CAClient class. */
	private CAClient caClient;
	
	private static final String WS_URL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";
	
	public static final String CONTEXT_PROPERTY = "my.property";
	public static final String CREDIT_CARD_NR = "creditCardNr";
	
	/** Certificate Authority Certificate. */
	public static final String CERTIFICATE = "T14_Mediator";
	
	/** Resource Path Certificate Authority Certificate. */
	public static final String CA_CERTIFICATE = "ca.cer";
	
	/** Resource Path Certificate. */
	//public static final String CERTIFICATE = "T14_Mediator.cer";
	
	/** keyStore and its attributes from the src/test/resources directory. */
	final static String KEYSTORE = "T14_Mediator.jks";
	final static String KEYSTORE_PASSWORD = "gXRhq5CQ";

	final static String KEY_ALIAS = "T14_Mediator";
	final static String KEY_PASSWORD = "gXRhq5CQ";
	
	
	//
	// Handler interface implementation
	//

	/**
	 * Gets the header blocks that can be processed by this Handler instance. If
	 * null, processes all.
	 */
	@Override
	public Set<QName> getHeaders() {
		return null;
	}
	
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("EncryptionHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		try {
			if(outboundElement.booleanValue()) {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				// check body
				SOAPBody sb = se.getBody();
				if (sb == null) {
					System.out.println("Body not found.");
					return true;
				}
				
				// get and cipher credit card number
				NodeList children = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node node = children.item(i);

					if(node.getNodeName().equals(CREDIT_CARD_NR)) {
						System.out.println("Encrypting CC Number in outbound SOAP message...");
						String creditCardNr = node.getTextContent();
						System.out.print("CreditCard Number: ");
						System.out.println(node.getTextContent());
						
						byte[] creditCardBytes = creditCardNr.getBytes();
						System.out.print("CreditCard Bytes: ");
						System.out.println(printBase64Binary(creditCardBytes));
						
						// Vai buscar chave a CA
						caClient = new CAClient(WS_URL);
						String certificateString = caClient.getCertificate(CERTIFICATE);
						Certificate cert = CertUtil.getX509CertificateFromPEMString(certificateString);
						
						// Certifica o certificado da CA
						Certificate caCertificate = CertUtil.getX509CertificateFromResource(CA_CERTIFICATE);
						boolean result = CertUtil.verifySignedCertificate(cert, caCertificate);
						System.out.print("Is certificate right:");
						System.out.println(result);
						if (!result)
							throw new RuntimeException();
							
						PublicKey publicKey = cert.getPublicKey();
						/*
						// Vai buscar chave ao certificado nos recursos
						Certificate cert = CertUtil.getX509CertificateFromResource(CERTIFICATE);
						PublicKey publicKey = cert.getPublicKey();
						*/
						byte[] cipheredBytes = cryptoUtil.asymCipher(creditCardBytes, publicKey);
						System.out.print("CreditCard Ciphered: ");
						
						String encodedCreditCard = Base64.getEncoder().encodeToString(cipheredBytes);
						System.out.println(encodedCreditCard);
						
						node.setTextContent(encodedCreditCard);
						msg.saveChanges();	
					}
				}	
			} else {
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();

				// check body
				SOAPBody sb = se.getBody();
				if (sb == null) {
					System.out.println("Body not found.");
					return true;
				}

				// get and decipher credit card number
				NodeList children = sb.getFirstChild().getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					Node node = children.item(i);
					
					if(node.getNodeName().equals(CREDIT_CARD_NR)) {
						System.out.println("Decrypting CC Number in inbound SOAP message...");
						String creditCardNr = node.getTextContent();
						
						byte[] creditCardBytes = Base64.getDecoder().decode(creditCardNr);
						
						// get private key from the example KeyStore
						PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(KEYSTORE, KEYSTORE_PASSWORD.toCharArray(), KEY_ALIAS, KEY_PASSWORD.toCharArray());
						
						System.out.println("Deciphering  with private key...");
						byte[] decipheredBytes = cryptoUtil.asymDecipher(creditCardBytes, privateKey); 
						System.out.println("Deciphered bytes:");
						System.out.println(printBase64Binary(decipheredBytes));
						
						System.out.print("CreditCardNumber: ");
						String CreditCardNr = new String(decipheredBytes);
						System.out.println(CreditCardNr);
						
						node.setTextContent(CreditCardNr);
						msg.saveChanges();	

						
					}
				}
			}
			
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}
		return true;
	}
	
	/** The handleFault method is invoked for fault message processing. */
	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	/**
	 * Called at the conclusion of a message exchange pattern just prior to the
	 * JAX-WS runtime dispatching a message, fault or exception.
	 */
	@Override
	public void close(MessageContext messageContext) {
		// nothing to clean up
	}
}



