package org.komparator.mediator.ws.it;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.komparator.mediator.ws.EmptyCart_Exception;
import org.komparator.mediator.ws.InvalidCartId_Exception;
import org.komparator.mediator.ws.InvalidCreditCard_Exception;
import org.komparator.mediator.ws.InvalidItemId_Exception;
import org.komparator.mediator.ws.InvalidQuantity_Exception;
import org.komparator.mediator.ws.ItemIdView;
import org.komparator.mediator.ws.NotEnoughItems_Exception;
import org.komparator.mediator.ws.cli.MediatorClient;
import org.komparator.mediator.ws.cli.MediatorClientException;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadProduct_Exception;
import org.komparator.supplier.ws.ProductView;

public class UpdateShopHistory extends BaseIT {
	
	private static final String VALID_CC = "1234567890123452";
	
	@Before
	public void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception, 
	MediatorClientException {
		// clear remote service state before each test
		mediatorClient.clear();
		
		// fill-in test products
		// (since addToCart is a read/write operation
		// the initialization below is done for each test)
		{
			ProductView prod = new ProductView();
			prod.setId("p1");
			prod.setDesc("AAA bateries (pack of 3)");
			prod.setPrice(3);
			prod.setQuantity(10);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p1");
			prod.setDesc("3batteries");
			prod.setPrice(4);
			prod.setQuantity(10);
			supplierClients[1].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p2");
			prod.setDesc("AAA bateries (pack of 10)");
			prod.setPrice(9);
			prod.setQuantity(20);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p2");
			prod.setDesc("10x AAA battery");
			prod.setPrice(8);
			prod.setQuantity(20);
			supplierClients[1].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p3");
			prod.setDesc("Digital Multimeter");
			prod.setPrice(15);
			prod.setQuantity(5);
			supplierClients[0].createProduct(prod);
		}

		{
			ProductView prod = new ProductView();
			prod.setId("p4");
			prod.setDesc("very cheap batteries");
			prod.setPrice(2);
			prod.setQuantity(5);
			supplierClients[0].createProduct(prod);
		}
	}
	
	@After
	public void tearDown() {
		// clear remote service state after each test
		mediatorClient.clear();
		// even though mediator clear should have cleared suppliers, clear them
		// explicitly after use
		supplierClients[0].clear();
		supplierClients[1].clear();
	}
	
	@Test
	public void addBuySleepAddBuyAgain() throws InvalidCartId_Exception, InvalidItemId_Exception, 
	InvalidQuantity_Exception, NotEnoughItems_Exception, InterruptedException, EmptyCart_Exception, InvalidCreditCard_Exception {
		System.out.println("Buying items first round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 1);
		}
		
		System.out.printf("%s going to sleep %n", UpdateCartIT.class);
		Thread.sleep(10000);
		System.out.printf("%s woke up!%n", UpdateCartIT.class);
		System.out.println("Buying items second round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("xyz", id, 10);
		}
		
		System.out.printf("%s going to sleep %n", UpdateCartIT.class);
		Thread.sleep(10000);
		System.out.printf("%s woke up!%n", UpdateCartIT.class);
		System.out.println("Buying items third round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 2);
		}
		
		mediatorClient.buyCart("xyz", VALID_CC);
		
		System.out.printf("%s going to sleep %n", UpdateCartIT.class);
		Thread.sleep(10000);
		System.out.printf("%s woke up!%n", UpdateCartIT.class);
		System.out.println("Buying items fourth round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p3");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("abc", id, 4);
		}
		
		mediatorClient.buyCart("abc", VALID_CC);
		
		System.out.println("Shutting down...");
	}
	
	@Test
	public void addBuyAddBuyAgain() throws InvalidCartId_Exception, InvalidItemId_Exception, 
	InvalidQuantity_Exception, NotEnoughItems_Exception, InterruptedException, EmptyCart_Exception, InvalidCreditCard_Exception {
		System.out.println("Buying items first round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 1);
		}
		
		System.out.println("Buying items second round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p2");
			id.setSupplierId(supplierNames[1]);
			mediatorClient.addToCart("xyz", id, 10);
		}
		
		System.out.println("Buying items third round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p1");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("xyz", id, 2);
		}
		
		mediatorClient.buyCart("xyz", VALID_CC);
		
		System.out.println("Buying items fourth round");
		
		{
			ItemIdView id = new ItemIdView();
			id.setProductId("p3");
			id.setSupplierId(supplierNames[0]);
			mediatorClient.addToCart("abc", id, 4);
		}
		
		mediatorClient.buyCart("abc", VALID_CC);
		
		System.out.println("Shutting down...");
	}
}
