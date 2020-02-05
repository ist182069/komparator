package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

import junit.framework.Assert;

/**
 * Test suite
 */
public class BuyProductIT extends BaseIT {
	
	private ProductView product;

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		{
			product = new ProductView();
			product.setId("X1");
			product.setDesc("Basketball");
			product.setPrice(10);
			product.setQuantity(10);
			client.createProduct(product);
		}
		{
			product = new ProductView();
			product.setId("Y2");
			product.setDesc("Baseball");
			product.setPrice(20);
			product.setQuantity(20);
			client.createProduct(product);
		}
		{
			product = new ProductView();
			product.setId("Z3");
			product.setDesc("Soccer ball");
			product.setPrice(30);
			product.setQuantity(30);
			client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
		client.clear();
	}

	// tests
	// assertEquals(expected, actual);

	// public String buyProduct(String productId, int quantity)
	// throws BadProductId_Exception, BadQuantity_Exception,
	// InsufficientQuantity_Exception {

	// bad input tests

	@Test(expected = BadProductId_Exception.class)
	public void buyProductNullTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct(null,1);
	}	
	
	@Test(expected = BadQuantity_Exception.class)
	public void buyProductBadQuantityTest_LessThanZero() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("Y2",-1);
	}
	
	@Test(expected = BadQuantity_Exception.class)
	public void buyProductBadQuantityTest_Zero() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("Y2",0);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductEmptyTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("",1);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductWhiteSpaceTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("    ",1);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductBadIdTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("\t\n\t\n",1);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTestImmediate() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("X1",11);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTestNextBuy_1() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("X1",8);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(2, client.getProduct("X1").getQuantity());
		
		client.buyProduct("X1", 4);
	}
	
	@Test(expected = InsufficientQuantity_Exception.class)
	public void buyProductInsufficientQuantityTestNextBuy_2() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		String purchaseId = client.buyProduct("X1",10);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(0, client.getProduct("X1").getQuantity());
		
		client.buyProduct("X1", 4);
	}
	
	@Test(expected = BadProductId_Exception.class)
	public void buyProductInexistentProductTest() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception {
		client.buyProduct("P3",11);
	}
	
	@Test(expected = BadProductId_Exception.class)
    public void buyProductTestLowKey_1() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
        client.buyProduct("y2",10);
    }

	@Test(expected = BadProductId_Exception.class)
    public void buyProductTestLowKey_2() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
        client.buyProduct("z3",10);
    }
	
    @Test(expected = BadProductId_Exception.class)
    public void buyProductTestLowKey_3() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
        client.buyProduct("p3",10);
    }
	
	// main tests
	
	@Test
	public void buyProductTest_Inside() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId = client.buyProduct("X1",8);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(2, client.getProduct("X1").getQuantity());
	}
	
	@Test
	public void buyProductTest_NearDownBorder() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId = client.buyProduct("X1",2);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(8, client.getProduct("X1").getQuantity());
	}
	
	@Test
	public void buyProductTest_NearUpperBorder() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId = client.buyProduct("X1",9);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(1, client.getProduct("X1").getQuantity());
	}
	
	@Test
	public void buyProductTest_MinQuantity() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId = client.buyProduct("X1",1);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(9, client.getProduct("X1").getQuantity());
	}
	
	@Test
	public void buyProductTest_MaxQuantity() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId = client.buyProduct("X1",10);
		
		assertNotNull(purchaseId);
		assertEquals(3, client.listProducts().size());
		assertEquals(1, client.listPurchases().size());
		
		assertEquals(0, client.getProduct("X1").getQuantity());
		
	}
	
	@Test
	public void buyProductTest_MoreThanOne() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId_1 = client.buyProduct("Y2",5);
		
		assertEquals(3, client.listProducts().size());
		
		assertNotNull(purchaseId_1);
		assertEquals(1, client.listPurchases().size());
		
		String purchaseId_2 = client.buyProduct("Z3",10);

		assertNotNull(purchaseId_2);		
		assertEquals(2, client.listPurchases().size());
		
		assertEquals(15, client.getProduct("Y2").getQuantity());
		assertEquals(20, client.getProduct("Z3").getQuantity());
		
	}
	
	@Test
	public void buyProductTest_DifferentPurchaseId() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		String purchaseId_1 = client.buyProduct("Y2",5);
		String purchaseId_2 = client.buyProduct("Z3",10);
		
		assertNotNull(purchaseId_1);	
		assertNotNull(purchaseId_2);
		
		assertEquals(3, client.listProducts().size()); //comprar várias vezes o mesmo producto e ver os purchases Ids e a lista de productos
		assertEquals(2, client.listPurchases().size());
		
		assertEquals(15, client.getProduct("Y2").getQuantity());
		assertEquals(20, client.getProduct("Z3").getQuantity());
		
		assertFalse(purchaseId_1.equals(purchaseId_2));
	}
	
	@Test
	public void buyProductTest_PurchaseSameProduct() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		assertEquals(3, client.listProducts().size());
		
		String purchaseId_1 = client.buyProduct("Y2",5);
		assertNotNull(purchaseId_1);
		assertEquals(1, client.listPurchases().size());
		assertEquals(15, client.getProduct("Y2").getQuantity());
		
		
		String purchaseId_2 = client.buyProduct("Y2",10);
		assertNotNull(purchaseId_2);
		assertEquals(2, client.listPurchases().size());	
		assertEquals(5, client.getProduct("Y2").getQuantity());
		
		assertFalse(purchaseId_1.equals(purchaseId_2));
	}
	
	@Test
	public void buyProductTest_AnnoyingPurchases() throws BadProductId_Exception, BadQuantity_Exception, InsufficientQuantity_Exception, BadProduct_Exception {
		assertEquals(3, client.listProducts().size());
		
		String purchaseId_1 = client.buyProduct("X1",1);
		assertNotNull(purchaseId_1);
		assertEquals(9, client.getProduct("X1").getQuantity());
		
		String purchaseId_2 = client.buyProduct("X1",5);
		assertNotNull(purchaseId_2);
		assertEquals(4, client.getProduct("X1").getQuantity());
		
		String purchaseId_3 = client.buyProduct("X1",4);
		assertNotNull(purchaseId_3);
		assertEquals(0, client.getProduct("X1").getQuantity());
		
		String purchaseId_4 = client.buyProduct("Y2",20);
		assertNotNull(purchaseId_4);
		assertEquals(0, client.getProduct("Y2").getQuantity());
		
		String purchaseId_5 = client.buyProduct("Z3",20);
		assertNotNull(purchaseId_5);
		assertEquals(10, client.getProduct("Z3").getQuantity());
		
		assertEquals(5, client.listPurchases().size());
		assertFalse(purchaseId_1.equals(purchaseId_2));
		
	} 

	
}
