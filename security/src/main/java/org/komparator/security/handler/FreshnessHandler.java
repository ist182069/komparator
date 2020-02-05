package org.komparator.security.handler;

import java.io.IOException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import java.time.LocalDateTime;

/**
 * This SOAPHandler does what is requested in the teacher's exercise.
 */
public class FreshnessHandler implements SOAPHandler<SOAPMessageContext> {
	
	public static final String CONTEXT_PROPERTY = "my.property";
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

	/**
	 * The handleMessage method is invoked for normal processing of inbound and
	 * outbound messages.
	 */
	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		System.out.println("AddHeaderHandler: Handling message.");

		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			if(outboundElement.booleanValue()) {
				System.out.println("Writing header in OUTbound SOAP message...");
				
				// get SOAP envelope
				SOAPMessage msg = smc.getMessage();
				SOAPPart sp = msg.getSOAPPart();
				SOAPEnvelope se = sp.getEnvelope();
				
				// add header
				SOAPHeader sh = se.getHeader();
				if (sh == null)
					sh = se.addHeader();
				
				////////////////////////////////////////////
				
				// TIMESTAMP HEADER 
				
				////////////////////////////////////////////
				
				
				// add header element (name, namespace prefix, namespace)
				Name name = se.createName("Timestamp", "t", "http://timestamp");
				SOAPHeaderElement element = sh.addHeaderElement(name);
				
				// getLocalDate
				Date date = new Date();
				String timestamp = dateFormatter.format(date);
				
				// add header element value
				element.addTextNode(timestamp);
				
				//////////////////////////////////////
				
				
				//////////////////////////////////////
				
				// NONCE HEADER 
					
				//////////////////////////////////////
				
				// add header element (name, namespace prefix, namespace)
				Name n = se.createName("NONCE", "n", "http://nonce");
				SOAPHeaderElement e = sh.addHeaderElement(n);
				
				
				
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

				System.out.println("Generating random byte array ...");

				final byte array[] = new byte[32];
				random.nextBytes(array);
				
				String nonce = Base64.getEncoder().encodeToString(array);
				
				e.addTextNode(nonce);
				
				//
				
				//////////////////////////////////////
				
				msg.saveChanges();
			
			} 
					
			
		} catch(Exception e) {
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

	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

}
