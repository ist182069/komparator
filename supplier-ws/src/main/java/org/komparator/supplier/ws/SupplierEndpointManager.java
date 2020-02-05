package org.komparator.supplier.ws;

import java.io.IOException;

import javax.xml.ws.Endpoint;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;


/** End point manager */
public class SupplierEndpointManager {
	
	/** Web Service location to publish */
	private String wsURL = null;
	
	/** UDDI location to publish */
	private String uddiURL = null;
	
	/** UDDI name to publish */
	private String uddiName = null;


	/** Port implementation */
	private SupplierPortImpl portImpl = new SupplierPortImpl(this);

// TODO
//	/** Obtain Port implementation */
//	public SupplierPortType getPort() {
//		return portImpl;
//	}

	/** Web Service end point */
	private Endpoint endpoint = null;
	
	/** Web Service UDDI Naming */
	private UDDINaming uddiNaming = null;

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL */
	public SupplierEndpointManager(String wsURL, String uddiURL, String uddiName) {
		if (wsURL == null)
			throw new NullPointerException("Web Service URL cannot be null!");
		if (uddiURL == null)
			throw new NullPointerException("UDDI URL cannot be null!");
		if (uddiName == null)
			throw new NullPointerException("UDDI name cannot be null!");
		
		this.wsURL = wsURL;
		this.uddiURL = uddiURL;
		this.uddiName = uddiName;
	}

	/* end point management */
	
	public String getWSName() {
		return this.uddiName;
	}

	public void start() throws Exception {
		try {
			/* e para receber o nome como no POM? */
			
			// publish end point
			endpoint = Endpoint.create(this.portImpl);
			if (verbose) {
				System.out.printf("Starting %s%n", wsURL);
			}
			endpoint.publish(wsURL);
			
			// publish to UDDI
			System.out.printf("Publishing '%s' to UDDI at %s%n", this.uddiName, uddiURL);
			uddiNaming = new UDDINaming(uddiURL);
			uddiNaming.rebind(this.uddiName, wsURL);
						
		} catch (Exception e) {
			endpoint = null;
			if (verbose) {
				System.out.printf("Caught exception when starting: %s%n", e);
				e.printStackTrace();
			}
			throw e;
		}
	}

	public void awaitConnections() {
		if (verbose) {
			System.out.println("Awaiting connections");
			System.out.println("Press enter to shutdown");
		}
		try {
			System.in.read();
		} catch (IOException e) {
			if (verbose) {
				System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
			}
		}
	}

	public void stop() throws Exception {
		try {
			if (endpoint != null) {
				// stop end point
				endpoint.stop();
				if (verbose) {
					System.out.printf("Stopped %s%n", wsURL);
				}
			}
		} catch (Exception e) {
			if (verbose) {
				System.out.printf("Caught exception when stopping: %s%n", e);
			}
		}
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(this.uddiName);
				System.out.printf("Deleted '%s' from UDDI%n", this.uddiName);
				uddiNaming = null;
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}
		this.portImpl = null;
	}

}
