package org.komparator.supplier.ws.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.komparator.supplier.ws.*;

/**
 * Test suite
 */
public class SearchProductsIT extends BaseIT {

	// static members

	// one-time initialization and clean-up
	@BeforeClass
	public static void oneTimeSetUp() throws BadProductId_Exception, BadProduct_Exception {
		client.clear();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		client.clear();
	}
	
	// members

	// initialization and clean-up for each test
	@Before
	public void setUp() throws BadProductId_Exception, BadProduct_Exception {
		
		{
		ProductView product = new ProductView();
		product.setId("X1");
		product.setDesc("Basketball");
		product.setPrice(10);
		product.setQuantity(10);
		client.createProduct(product);
		}
		{	
		ProductView product = new ProductView();
		product.setId("Y2");
		product.setDesc("Baseball");
		product.setPrice(20);
		product.setQuantity(20);
		client.createProduct(product);	
		}
		{	
		ProductView product = new ProductView();
		product.setId("Z3");
		product.setDesc("Soccer ball");
		product.setPrice(30);
		product.setQuantity(30);
		client.createProduct(product);
		}
	}

	@After
	public void tearDown() {
	}

	// bad input tests
	@Test(expected = BadText_Exception.class)
	public void searchProductNullTest() throws BadText_Exception {
		client.searchProducts(null);
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductEmptyTest() throws BadText_Exception {
		client.searchProducts("");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductSpacesTest() throws BadText_Exception {
		client.searchProducts("    ");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductTabTest() throws BadText_Exception {
		client.searchProducts("\t");
	}
	
	@Test(expected = BadText_Exception.class)
	public void searchProductNewlineTest() throws BadText_Exception {
		client.searchProducts("\n");
	}

		
	// main tests
	@Test
	public void searchProductListFoundTest() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{

		List<ProductView> pv = client.searchProducts("Basketball");
		
		assertFalse(client.listProducts().size() == pv.size());
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 1);
		assertFalse("Empty", pv.isEmpty());
		assertNotNull(pv);
		
		assertEquals(pv.get(0).getDesc(), "Basketball");
		assertEquals(pv.get(0).getId(), "X1");
		assertEquals(pv.get(0).getPrice(), 10);
		assertEquals(pv.get(0).getQuantity(), 10);
		
	}
	
	@Test
	public void searchProductListFound2Test() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{

		List<ProductView> pv = client.searchProducts("Baseball");
		
		assertFalse(client.listProducts().size() == pv.size());
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 1);
		assertFalse("Empty", pv.isEmpty());
		assertNotNull(pv);
		
		assertEquals(pv.get(0).getDesc(), "Baseball");
		assertEquals(pv.get(0).getId(), "Y2");
		assertEquals(pv.get(0).getPrice(), 20);
		assertEquals(pv.get(0).getQuantity(), 20);

	}
	
	@Test
	public void searchProductListFound3Test() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{

		List<ProductView> pv = client.searchProducts("Soccer ball");
		
		assertFalse(client.listProducts().size() == pv.size());
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 1);
		assertFalse("Empty", pv.isEmpty());
		assertNotNull(pv);
		
		assertEquals(pv.get(0).getDesc(), "Soccer ball");
		assertEquals(pv.get(0).getId(), "Z3");
		assertEquals(pv.get(0).getPrice(), 30);
		assertEquals(pv.get(0).getQuantity(), 30);

	}
	
	
	@Test
	public void searchProductListCheckListTest() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{
				
		List<ProductView> pv = client.searchProducts("e");
		
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 3);
		assertFalse("Empty", pv.isEmpty());
		assertNotNull(pv);
		assertEquals(pv.get(0).getDesc(), "Basketball");
		assertEquals(pv.get(1).getDesc(), "Baseball");
		assertEquals(pv.get(2).getDesc(), "Soccer ball");
	}
	
	@Test
	public void searchProductListPartialTest() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{
		
		
		List<ProductView> pv = client.searchProducts("ask");
		
		assertFalse(client.listProducts().size() == pv.size());
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 1);
		assertNotNull(pv);
	}
	
	@Test
	public void searchProductListPartialNotFoundTest() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{
		  
		
		List<ProductView> pv = client.searchProducts("bsk");
		
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 0);
		assertTrue("Empty", pv.isEmpty());
		assertNotNull(pv);
	}
	
	
	@Test
	public void searchProductListCaseNotFoundTest() throws BadText_Exception, BadProductId_Exception,
	BadProduct_Exception{
		  
		List<ProductView> pv = client.searchProducts("BASKET");
		
		assertEquals(client.listProducts().size(), 3);
		assertEquals(pv.size(), 0);
		assertTrue("Empty", pv.isEmpty());
		assertNotNull(pv);
	}
		
}
