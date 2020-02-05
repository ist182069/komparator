package org.komparator.security.handler;



import java.util.Iterator;

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

import javax.xml.ws.handler.MessageContext.Scope;

import javax.xml.ws.handler.soap.SOAPHandler;

import javax.xml.ws.handler.soap.SOAPMessageContext;



import org.w3c.dom.Node;

import org.w3c.dom.NodeList;



public class NonceHandler implements SOAPHandler<SOAPMessageContext> {

    

    public static final String CART_UPDATED_NONCE = "cart.updated.nonce";
    
    public static final String SHOP_UPDATED_NONCE = "shop.updated.nonce";

    

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

		System.out.println("NonceHandler: Handling message.");



		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {

			if (outboundElement.booleanValue()) {

				System.out.println("Writing Nonce in header in outbound SOAP message...");

				// get SOAP envelope

				SOAPMessage msg = smc.getMessage();

				SOAPPart sp = msg.getSOAPPart();

				SOAPEnvelope se = sp.getEnvelope();

				SOAPBody sb = se.getBody();

				if(sb.getFirstChild().getNodeName().equals("ns2:addToCart")) {
					
					String nonceString = (String) smc.get(CART_UPDATED_NONCE);
					
					// add header

					SOAPHeader sh = se.getHeader();

					if (sh == null)

						sh = se.addHeader();

	

					// add header element (name, namespace prefix, namespace)

					Name nonce = se.createName("nonce", "n", "http://nonce");

					SOAPHeaderElement nonceElement = sh.addHeaderElement(nonce);

					// add header element value

					nonceElement.addTextNode(nonceString);

					msg.saveChanges();

				} else if(sb.getFirstChild().getNodeName().equals("ns2:buyCart")) {
					
					String nonceString = (String) smc.get(SHOP_UPDATED_NONCE);

					// add header

					SOAPHeader sh = se.getHeader();

					if (sh == null)

						sh = se.addHeader();

	

					// add header element (name, namespace prefix, namespace)

					Name nonce = se.createName("nonce", "n", "http://nonce");

					SOAPHeaderElement nonceElement = sh.addHeaderElement(nonce);

					// add header element value

					nonceElement.addTextNode(nonceString);

					msg.saveChanges();

				}

			}

			else {

				System.out.println("Reading Nonce in header in inbound SOAP message...");

				// get SOAP envelope

				SOAPMessage msg = smc.getMessage();

				SOAPPart sp = msg.getSOAPPart();

				SOAPEnvelope se = sp.getEnvelope();

				SOAPBody sb = se.getBody();

				if(sb.getFirstChild().getNodeName().equals("ns2:addToCart")) {

					SOAPHeader sh = se.getHeader();

					NodeList children = sh.getChildNodes();

					

					String nonce = "";

					for (int i = 0; i < children.getLength(); i++) {

						Node node = children.item(i);



						if(node.getNodeName().equals("n:nonce")) {

							nonce = node.getTextContent();

						}

					}

					

					smc.put(CART_UPDATED_NONCE, nonce);

					// set property scope to application so that server class can

					// access property

					smc.setScope(CART_UPDATED_NONCE, Scope.APPLICATION);

				} else if(sb.getFirstChild().getNodeName().equals("ns2:buyCart")) {
					
					SOAPHeader sh = se.getHeader();

					NodeList children = sh.getChildNodes();

					

					String nonce = "";

					for (int i = 0; i < children.getLength(); i++) {

						Node node = children.item(i);



						if(node.getNodeName().equals("n:nonce")) {

							nonce = node.getTextContent();

						}

					}

					smc.put(SHOP_UPDATED_NONCE, nonce);

					// set property scope to application so that server class can

					// access property

					smc.setScope(SHOP_UPDATED_NONCE, Scope.APPLICATION);
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

