package org.komparator.mediator.ws.it;

import org.junit.Test;

import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;

/**
 * Test suite
 */
public class ImAliveIT extends BaseIT {
	
	private static final String SECUNDARY_URL = "http://localhost:8072/mediator-ws/endpoint";
	
	@Test
	public void imAlivePrimaryMediatorTest() {
		mediatorClient.imAlive();
	}
	
	@Test
	public void imAliveSecundaryMediatorTest() {
		MediatorClient secundaryClient;
		try {
			secundaryClient = new MediatorClient(SECUNDARY_URL);
			secundaryClient.imAlive();
		} catch (MediatorClientException e) {
			System.out.print("Caught exception in imAliveSecundaryMediatorTest: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		} catch (Exception e) {
			System.out.println("Secundary Mediator not there");
			System.out.println("Continue normal processing...");
		}
	}
}
