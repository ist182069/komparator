package org.komparator.mediator.ws;

import java.util.Timer;

import com.sun.xml.ws.wsdl.writer.document.Service;

public class MediatorApp {
	// alterar aqui
	private static final String MAIN_PORT = "http://localhost:8071/mediator-ws/endpoint";
	
	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length == 0 || args.length == 2) {
			System.err.println("Argument(s) missing!");
			System.err.println("Usage: java " + MediatorApp.class.getName() + " wsURL OR uddiURL wsName wsURL");
			return;
		}
		String uddiURL = null;
		String wsName = null;
		String wsURL = null;

		// Create server implementation object, according to options
		MediatorEndpointManager endpoint = null;
		if (args.length == 1) {
			wsURL = args[0];
			endpoint = new MediatorEndpointManager(wsURL);
		} else if (args.length >= 3) {
			uddiURL = args[0];
			wsName = args[1];
			wsURL = args[2];
			if (wsURL.equals(MAIN_PORT)) {
				System.out.println("Starting out primary Mediator Service...");
				endpoint = new MediatorEndpointManager(uddiURL, wsName, wsURL);
				endpoint.setVerbose(true);
			}
			else {
				System.out.println("Starting out secundary (backup) Mediator Service...");
				endpoint = new MediatorEndpointManager(wsURL);
			}
			// create timer object
	        // set it as a daemon so the JVM doesn't wait for it when quitting
	        Timer timer = new Timer(/*isDaemon*/ true);
	        // create life proof object
	        LifeProof lifeProof = new LifeProof(endpoint, wsURL, uddiURL);
	        // schedule the life proof sending signal
	        timer.schedule(lifeProof, /*delay*/ 5 * 1000, /*period*/ 5 * 1000);
		}       
        
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
