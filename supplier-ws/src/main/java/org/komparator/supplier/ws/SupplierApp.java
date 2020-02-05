package org.komparator.supplier.ws;

/** Main class that starts the Supplier Web Service. */
public class SupplierApp {

	public static void main(String[] args) throws Exception {
		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsURL%n", SupplierApp.class.getName());
			return;
		}
		
		/* e suposto receber como nome nos argumentos da pom? duvida no endpoint */
		String wsURL = args[0];
		String uddiURL = args[1];
		String uddiName = args[2];

		// Create server implementation object
		SupplierEndpointManager endpoint = new SupplierEndpointManager(wsURL, uddiURL, uddiName);
		try {
			endpoint.start();
			endpoint.awaitConnections();
		} finally {
			endpoint.stop();
		}

	}

}
