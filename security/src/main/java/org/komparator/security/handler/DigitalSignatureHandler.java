package org.komparator.security.handler;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.lang.RuntimeException;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import org.komparator.security.CertUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pt.ulisboa.tecnico.sdis.ws.cli.CAClient;
import java.text.SimpleDateFormat;

public class DigitalSignatureHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.property";
	
	public static final String PRODUCT_ID = "productId";
	public static final String DESC_TEXT = "descText";
	public static final String QUANTITY = "quantity";

	/** Digital signature algorithm. */
	private static final String SIGNATURE_ALGO = "SHA256withRSA";
	
	public static final String CERTIFICATE = "T14_Mediator.cer";
	
	/** keyStore and its attributes from the src/test/resources directory. */
	final static String KEYSTORE_PASSWORD = "gXRhq5CQ";

	final static String KEY_PASSWORD = "gXRhq5CQ";
	
	public static final String CA_CERTIFICATE = "ca.cer";

	protected static Properties testProps;
	
	/** CAClient class. */
	private CAClient caClient;
	
	private static final String WS_URL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8081/ca";
	
	private static List<String> nonceList =  new ArrayList();
	
	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
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
		System.out.println("DigitalSignatureHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		try {
			if(outboundElement.booleanValue()) {
				// get SOAP envelope
				System.out.println("Handling OUTbound message...");
				
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				// check body
				SOAPBody sb = se.getBody();
				if (sb == null) {
					System.out.println("Body not found.");
					return true;
				}
			
				// Get body
				String textContent = se.getTextContent();
				System.out.println("textContent: " + textContent);
				byte[] plainBytes = textContent.getBytes();
				System.out.println("plainBytes: " + printHexBinary(plainBytes));

				// check header
				SOAPHeader sh = se.getHeader();
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}

				// get first header element
				Name wsn_name = se.createName("wsName", "wsn", "WebServiceNameNS");
				Iterator it = sh.getChildElements(wsn_name);
				// check header element
				if (!it.hasNext()) {
					System.out.println("Header element not found.");
					return true;
				}
				SOAPElement wsn_element = (SOAPElement) it.next();

				// get header element value
				String wsName = wsn_element.getValue();
				String keyStore = wsName + ".jks";

				// make digital signature
				System.out.println("Signing ...");
				PrivateKey privateKey = CertUtil.getPrivateKeyFromKeyStoreResource(keyStore,
					KEYSTORE_PASSWORD.toCharArray(), wsName, KEY_PASSWORD.toCharArray());
				byte[] digitalSignature = CertUtil.makeDigitalSignature(SIGNATURE_ALGO, privateKey, plainBytes);
						
				String textDigitalSignature = Base64.getEncoder().encodeToString(digitalSignature);
								
				// add header element (name, namespace prefix, namespace)
				Name dig_name = se.createName("Digest", "dig", "DigestNameSpace");
				SOAPHeaderElement dig_element = sh.addHeaderElement(dig_name);
				
				// add header element value
				dig_element.addTextNode(textDigitalSignature);
						
				msg.saveChanges();	
						
				}	
			 else { 
				System.out.println("Handling INbound message...");
				
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
							
				// check header
				SOAPHeader sh = se.getHeader();
				if (sh == null) {
					System.out.println("Header not found.");
					return true;
				}
				 
				// get id and digest
				NodeList children = sh.getChildNodes();
				// id and digest empty strings
				String id = "";
				String digest = "";
				String nonce = "";
				String timestamp = "";

				for (int i = 0; i < children.getLength(); i++) {
					Node node = children.item(i);

					if(node.getNodeName().equals("wsn:wsName")) {
						id = node.getTextContent();
					}
					if(node.getNodeName().equals("dig:Digest")) {
						digest = node.getTextContent();
						//remove unwanted header
						sh.removeChild(node);
					}
					if(node.getNodeName().equals("n:NONCE")) {
						nonce = node.getTextContent();
					}
					if(node.getNodeName().equals("t:Timestamp")) {
						timestamp = node.getTextContent();
					}
				}
				
				if (digest.equals("")) {
					System.out.println("Id not found.");
					return true;
				}
				if(digest.equals("")) {
					System.out.println("Digest not found.");
					return true;
				}
				if(nonce.equals("")) {
					System.out.println("NONCE not found.");
					return true;
				}
				if(timestamp.equals("")) {
					System.out.println("Timestamp not found.");
					return true;
				}
								
				byte[] digitalSignature = Base64.getDecoder().decode(digest);
				
				
				// get Certificate
				caClient = new CAClient(WS_URL);
				String certificateString = caClient.getCertificate(id);
				
				// Certifica o certificado da CA
				Certificate caCertificate = CertUtil.getX509CertificateFromResource(CA_CERTIFICATE);
				Certificate cert = CertUtil.getX509CertificateFromPEMString(certificateString);
				boolean verifiedCertificate = CertUtil.verifySignedCertificate(cert, caCertificate);
				System.out.print("Is certificate right:");
				System.out.println(verifiedCertificate);
				if (!verifiedCertificate)
					throw new RuntimeException();
				
				// get Body Content
				String plainText = se.getTextContent();
				byte[] plainBytes = plainText.getBytes();
				
				// verify the signature
				System.out.println("Verifying ...");
				PublicKey publicKey = CertUtil.getX509CertificateFromPEMString(certificateString).getPublicKey();
				boolean result = CertUtil.verifyDigitalSignature(SIGNATURE_ALGO, publicKey, plainBytes, digitalSignature);
				System.out.print("Is signature right: ");
				System.out.println(result);
				
				if (!result) {
					throw new RuntimeException();
				}
				
				// timestamp difference verify
				Date date_recv = dateFormatter.parse(timestamp);
				Date date_now = new Date();
				
				System.out.print("Time elapsed between the sending and receiving of the SOAP mesage: ");
				System.out.println(date_now.getTime() - date_recv.getTime());
				
				//test if time difference is bigger than half second
				if(date_now.getTime() - date_recv.getTime() > 3000) {
					throw new RuntimeException();
				}
				
				// testar se nonce ja foi visto
				for( String n : nonceList){
					if(n.equals(nonce)){
						throw new RuntimeException();
					}
				}
				
				// adicionar a lista de nonces
				nonceList.add(nonce);
				
				System.out.print("Nonce List:");
				System.out.println(nonceList);


			}	
			
		}
			
			
		 catch (Exception e) {
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

