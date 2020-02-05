package org.komparator.mediator.ws;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.komparator.mediator.domain.Cart;
import org.komparator.mediator.domain.Item;
import org.komparator.mediator.domain.Mediator;
import org.komparator.security.handler.NonceHandler;
import org.komparator.supplier.ws.BadProductId_Exception;
import org.komparator.supplier.ws.BadQuantity_Exception;
import org.komparator.supplier.ws.BadText_Exception;
import org.komparator.supplier.ws.InsufficientQuantity_Exception;
import org.komparator.supplier.ws.ProductView;
import org.komparator.supplier.ws.cli.SupplierClient;
import org.komparator.supplier.ws.cli.SupplierClientException;

import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClient;
import pt.ulisboa.tecnico.sdis.ws.cli.CreditCardClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

@WebService(
		endpointInterface = "org.komparator.mediator.ws.MediatorPortType", 
		wsdlLocation = "mediator.1_0.wsdl", 
		name = "MediatorWebService", 
		portName = "MediatorPort", 
		targetNamespace = "http://ws.mediator.komparator.org/", 
		serviceName = "MediatorService"
)

@HandlerChain(file = "/mediator-ws_handler-chain.xml")

public class MediatorPortImpl implements MediatorPortType {
	
	@Resource
    private WebServiceContext webServiceContext;
	
	/** Date formatter used for outputting timestamps in ISO 8601 format */
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private AtomicInteger shoppingResultViewId = new AtomicInteger(0);
	
	private String lastRegisteredNonce = ""; 
	
	private String lastRegisteredShopNonce = ""; 
	/*
	private long regularDelay = 5000;
	
	private long delayMargin = 2500;
	*/
	private Date prevDate = null;
	
	public static String cartUpdateNonce;
	
	public static String shopResultsUpdateNonce;
	
	public static String lastRegisteredTimestamp; 
	
	public static boolean primaryOnline = false;
	// passou a ser synchronized, sera que podemos assumir que e so um cliente? porque isto faz a cena ser so para um
	public List<ShoppingResultView> shopResults = new ArrayList<ShoppingResultView>();
	
	public static List<ShoppingResultView> lastRegisteredShopResults = Collections.synchronizedList(new ArrayList<ShoppingResultView>());
	// Todos os carrinhos virtualizados ate agora
	public static List<CartView> lastRegisteredCartViews = Collections.synchronizedList(new ArrayList<CartView>());
	//public static CartView lastRegisteredCartView = null;
	
	public static final Object lockCartView = new Object();
	
	public static final Object lockShopResults = new Object();
	
	/* public static long networkDelay = 0; */
	
	public static boolean flag = false;
	
	// end point manager
	private MediatorEndpointManager endpointManager;

	public MediatorPortImpl(MediatorEndpointManager endpointManager) {
		this.endpointManager = endpointManager;
	}

	
	// Main operations -------------------------------------------------------
	@Override
	public List<ItemView> getItems(String productId) throws InvalidItemId_Exception {
		if(productId == null)
			throwInvalidItemId("ProductId cannot be null");
		
		productId = productId.trim();
		if(productId.length() == 0)
			throwInvalidItemId("ProductId cannot be empty or whitespace!");
		
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		try {
			
			Collection<UDDIRecord> wsURLList = uddiNaming.listRecords("T14_Supplier%");
			List<ItemView> itemViewList = new ArrayList<ItemView>();
		
			for(UDDIRecord iteratorString : wsURLList) {
				if(iteratorString == null || iteratorString.getUrl() == null || iteratorString.getUrl() == "" 
						|| iteratorString.getUrl().trim() == "")
					break;
				
				SupplierClient supplierClient = new SupplierClient(iteratorString.getUrl());
				ProductView productView = supplierClient.getProduct(productId);
				
				if(productView == null)
					continue;
				
				ItemView itemView = new ItemView();
				ItemIdView itemIDView = new ItemIdView();
					
				itemIDView.setProductId(productView.getId());
				itemIDView.setSupplierId(iteratorString.getOrgName());
					
				itemView.setItemId(itemIDView);
				itemView.setDesc(productView.getDesc());
				itemView.setPrice(productView.getPrice());
					
				itemViewList.add(itemView);		
			}
			
			Comparator<ItemView> comparator = new Comparator<ItemView>() {
				@Override
				public int compare(ItemView pv_1, ItemView pv_2) {
					return Integer.compare(pv_1.getPrice(), pv_2.getPrice());
				}
			};
			
			Collections.sort(itemViewList, comparator);
			
			return itemViewList;
					
		} catch (BadProductId_Exception e) {
			System.out.println("Error on Product ID: " + e);
		} catch (SupplierClientException e) {
			System.out.println("Error on dealing with Supplier Client: " + e);
		} catch (UDDINamingException e) {
			System.out.println("Error on dealing with UDDI: " + e);
		}
		return null;
	}
	
	@Override
	public List<ItemView> searchItems(String descText) throws InvalidText_Exception {
		if(descText == null)
			throwInvalidText("Description cannot be null");
		
		descText = descText.trim();
		if(descText.length() == 0)
			throwInvalidText("Description cannot be empty or whitespace!");		
		
        UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		List<ItemView> itemViewList = new ArrayList<ItemView>(); //usar o mesmo metodo para todos
		Collection<UDDIRecord> namingList;
		try {
			namingList = uddiNaming.listRecords("T14_Supplier%");
			for(UDDIRecord string : namingList) {
			               
				if(string == null || string.getUrl() == null || string.getUrl() == "" 
						|| string.getUrl().trim() == "")
					break;
				SupplierClient supplierClient = new SupplierClient(string.getUrl());
				List<ProductView> list = supplierClient.searchProducts(descText);
						
				for(ProductView productView: list){
					ItemView itemView = new ItemView();
					ItemIdView itemIDView = new ItemIdView();
							
					itemIDView.setProductId(productView.getId());
					itemIDView.setSupplierId(string.getOrgName());
							
					itemView.setItemId(itemIDView);
					itemView.setDesc(productView.getDesc());
					itemView.setPrice(productView.getPrice());
							
					itemViewList.add(itemView);
				}
			} 
			
			} catch (BadText_Exception e) {
				System.out.println("Error on received text: " + e);
			} catch (SupplierClientException e) {
				System.out.println("Error on dealing with Supplier Client: " + e);
			} catch (UDDINamingException e) {
				System.out.println("Error on dealing with UDDI: " + e);
			}
	      	
			Comparator<ItemView> comparator = new Comparator<ItemView>() { 
				public int compare(ItemView pv_1, ItemView pv_2) { 
					int result = pv_1.getItemId().getProductId().compareToIgnoreCase(pv_2.getItemId().getProductId()); 
					if(result != 0){ 
						return result; 
					}
					else{ 
						return ((Integer)pv_1.getPrice()).compareTo(pv_2.getPrice()); 
						} 
					}
			};
	      	
	      	Collections.sort(itemViewList, comparator);
			
			return itemViewList;
		
	}
	
	@Override
	public synchronized void addToCart(String cartId, ItemIdView itemIdView, int itemQty) throws InvalidCartId_Exception, InvalidItemId_Exception, InvalidQuantity_Exception, NotEnoughItems_Exception {
		if(cartId == null) 
			throwInvalidCartId("Cart ID cannot be null");
		cartId = cartId.trim(); 
		if(cartId.length() == 0) 
			throwInvalidCartId("Cart ID cannot be empty or whitespace!");

		if(itemIdView == null) 
			throwInvalidItemId("Item ID View cannot be null");
		if(itemQty <= 0) 
			throwInvalidQuantity("Quantity cannot be negative!");
		
		if(itemIdView.getProductId() == null) 
			throwInvalidItemId("Product ID cannot be null!");
				
		if(itemIdView.getSupplierId() == null) 
			throwInvalidItemId("Supplier ID cannot be null!");

		if(itemIdView.getProductId().trim().length() == 0) 
			throwInvalidItemId("Product ID is empty!");

		if(itemIdView.getSupplierId().trim().length() == 0) 
			throwInvalidItemId("Supplier ID is empty!");
		
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		//flag = true;
		//System.exit(0); //CRASHA ISTO TUDO
		MessageContext messageContext = webServiceContext.getMessageContext();
		
		String propertyValue = (String) messageContext.get(NonceHandler.CART_UPDATED_NONCE);
		System.out.println(propertyValue);
		if(!propertyValue.equals(lastRegisteredNonce) || propertyValue != null) {
			try {
				Collection<UDDIRecord> namingList = uddiNaming.listRecords("T14_Supplier%");
				for(UDDIRecord uddiRecord : namingList) {
					if(uddiRecord == null || uddiRecord.getUrl() == null || uddiRecord.getUrl() == "" 
							|| uddiRecord.getUrl().trim() == "") {
						break;	
					}
					
					SupplierClient supplierClient = new SupplierClient(uddiRecord.getUrl());
	
					if(uddiRecord.getOrgName().equals(itemIdView.getSupplierId())) {
						ProductView productView = supplierClient.getProduct(itemIdView.getProductId());
						
						if(productView == null)
							throwInvalidItemId("There is no such Item!");
							// verificar quantidade de um item nesse carro
							int quantityInCart = 0;
											
							for(CartView cartView : listCarts()) {
								if(cartView.getCartId().equals(cartId)) {
									for(CartItemView cartItemView : cartView.getItems()) {
										if(cartItemView.getItem().getItemId().getProductId().equals(itemIdView.getProductId())){
											quantityInCart+=cartItemView.getQuantity();
										}
									}
								}
							}
							
							if(productView.getQuantity() - quantityInCart >= itemQty) {
		 						Mediator mediator = Mediator.getInstance();
		 						mediator.addToCart(cartId, itemIdView.getProductId(), itemIdView.getSupplierId(), productView.getPrice(), productView.getDesc(), itemQty);
		 						/*
		 						// Apos adicionar uma dada quantidade ao carrinho, obter a sua virtualizacao
		 						lastRegisteredCartView = listCarts(); // adicionalmente, secalhar aqui era um synchronize e depois na thread outro segundo a logica actual
		 						// - O meu problema aqui e a sincronizacao pois esta lista vai ser acedida,
		 						//   ao mesmo tempo que a thread do LifeProof vai envia-la para o secundario.
		 						// - Ja agora, isto esta a ser feito no addCart pois sempre que adicionamos ao
		 						//   ao carrinho ele virtualiza de forma a acrescentar a variavel global que e
		 						//   acedida no LifeProof. O problema com isto e, nao valia mais a pena, fazer
		 						//   o metodo listCarts estatico?? Isto e, assim poderiamos usa-lo na thread
		 						//   do primario do LifeProof fazendo MediatorPortImpl.listItems() e assim
		 						//   nao estariamos dependentes de quando um item para chamar a listCarts de forma 
		 						//   a obter a virtualizacao do carrinho fisico. 
		 						//   Assim podemos estar a ir buscar constantemente os carrinhos de 5 em 5 segundos
		 						//   na thread do principal. O problema e que isto e um metodo estatico que e do 
		 						//   webService... Nao sei se da para definir coisas estaticas em contract-first
		 						//   no WSDL e pior, nao sei se isto funcionaria como deve ser.
		 						*/
		 						
		 						
		 						synchronized (lockCartView) {
		 							CartView cartView = new CartView();
		 							cartView.setCartId(cartId);
		 							
		 							CartItemView cartItemView = new CartItemView();
		 							cartItemView.setQuantity(itemQty);
		 							
		 							cartView.getItems().add(cartItemView);
		 							
		 							ItemView itemView = new ItemView();
		 							itemView.setDesc(productView.getDesc());
		 							itemView.setPrice(productView.getPrice());
		 							itemView.setItemId(itemIdView);
		 							
		 							cartItemView.setItem(itemView);
		 							
		 							lastRegisteredCartViews.add(cartView);
									
		 							lastRegisteredNonce = propertyValue;
									cartUpdateNonce =  lastRegisteredNonce;
									
									System.out.println("entra aqui");
									for(CartView cartView_ex : listCarts()) {
										for(CartItemView cartItemView_ex : cartView_ex.getItems()) {
											ItemView itemView_ex = cartItemView_ex.getItem();
											ItemIdView itemIdView_ex = itemView_ex.getItemId();
											System.out.println("");
											System.out.println("#######################");
											System.out.print("CartId: ");
											System.out.println(cartView_ex.getCartId());
											String productId_ex = itemIdView_ex.getProductId();
											String supplierId_ex = itemIdView_ex.getSupplierId();
											System.out.print("ProductId: ");
											System.out.println(productId_ex);
											String desc_ex = itemView.getDesc();
											System.out.print("SupplierId: ");
											System.out.println(supplierId_ex);
											System.out.print("Description: ");
											System.out.println(desc_ex);
											int price_ex = itemView_ex.getPrice();
											int itemQty_ex = cartItemView_ex.getQuantity();
											System.out.print("Quantidade: ");
											System.out.println(itemQty_ex);
											System.out.print("Price: ");
											System.out.println(price_ex);
											System.out.println("#######################");
											System.out.println("");
										}
									} 
		 						
							}
						}	
						else {
							throwNotEnoughItems("Quantity asked exceeds Item stock!");
						}
					} 
					
					else 
						continue;	
				}
			} catch (BadProductId_Exception e) {
				System.out.println("Error on Product ID: " + e);
			} catch (SupplierClientException e) {
				System.out.println("Error on dealing with Supplier Client: " + e);
			} catch (UDDINamingException e) {
				System.out.println("Error on dealing with UDDI: " + e);
			}
		}
		else {
			System.out.println("Pedido já recebido da ultima vez");
		}
		
	}	
	
	@Override
	public synchronized ShoppingResultView buyCart(String cartId, String creditCardNr)
            throws EmptyCart_Exception, InvalidCartId_Exception, InvalidCreditCard_Exception {
		
		if(cartId == null) 
			throwInvalidCartId("Cart ID cannot be null");
		
		cartId = cartId.trim(); 
		if(cartId.length() == 0) 
			throwInvalidCartId("Cart ID cannot be empty or whitespace!");
		
		boolean cartExists = false;
		
		for(CartView cartView : listCarts()) {
			if(cartView.getCartId().equals(cartId)) {
				cartExists = true;
				break;
			}
		}
		
		if(!cartExists)
			throwInvalidCartId("There is no such Cart!");
		
		if(creditCardNr == null) 
			throwInvalidCreditCard("Credit Card cannot be null");
		
		creditCardNr = creditCardNr.trim(); 
		if(creditCardNr.length() == 0) 
			throwInvalidCreditCard("Credit Card cannot be empty or whitespace!");
		
		if(creditCardNr.length() != 16) 
			throwInvalidCreditCard("Credit Card must have 16 digits!");
		
		MessageContext messageContext = webServiceContext.getMessageContext();
		
		String propertyValue = (String) messageContext.get(NonceHandler.SHOP_UPDATED_NONCE);
		System.out.println(propertyValue);
		
		int totalprice = 0;
		
		
		ShoppingResultView shoppingResultView = new ShoppingResultView();
		
		if(!propertyValue.equals(lastRegisteredShopNonce) || propertyValue != null) {
			for(CartView cartView : listCarts()) {
				
				if(cartView.getCartId().equals(cartId)) {
					
					if(cartView.getItems().size() == 0)
						throwEmptyCart("Cart is Empty");
					for(CartItemView cartItemView : cartView.getItems()) {
						UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
						
						try {
	
							String uddiURL = uddiNaming.lookup(cartItemView.getItem().getItemId().getSupplierId());
	
							CreditCardClient creditCardClient = new CreditCardClient("http://ws.sd.rnl.tecnico.ulisboa.pt:8080/cc");
	
							if(creditCardClient.validateNumber(creditCardNr)) {
								SupplierClient supplierClient = new SupplierClient(uddiURL);
	
								String productId = cartItemView.getItem().getItemId().getProductId();
								int quantity = cartItemView.getQuantity();
								
								supplierClient.buyProduct(productId, quantity);
	
								shoppingResultView.getPurchasedItems().add(cartItemView);
	
							}
							
							
						} catch(UDDINamingException e) {
							System.out.println("Error on dealing with UDDI: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						} catch (CreditCardClientException e) {
							System.out.println("Error on dealing with Credit Card: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						} catch (SupplierClientException e) {
							System.out.println("Error on dealing with Supplier Client: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						} catch (BadProductId_Exception e) {
							System.out.println("Error on Product ID: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						} catch (BadQuantity_Exception e) {
							System.out.println("Error on Quantity: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						} catch (InsufficientQuantity_Exception e) {
							System.out.println("Insufficient Quantity: " + e);
							shoppingResultView.getDroppedItems().add(cartItemView);
							continue;
						}
					}
					break;
				}
			}
			
	
			for(CartItemView cartItemView : shoppingResultView.getPurchasedItems()) {
				int quantity = cartItemView.getQuantity();
				int price = cartItemView.getItem().getPrice();
				
				totalprice = totalprice + quantity * price;
			}
			
			shoppingResultView.setId(Integer.toString(shoppingResultViewId.incrementAndGet()));
			shoppingResultView.setTotalPrice(totalprice);
			
			if(!shoppingResultView.getPurchasedItems().isEmpty() && shoppingResultView.getDroppedItems().isEmpty()) {
				shoppingResultView.setResult(Result.COMPLETE);
			}
			
			else if(shoppingResultView.getPurchasedItems().isEmpty() && !shoppingResultView.getDroppedItems().isEmpty()) {
				shoppingResultView.setResult(Result.EMPTY);
			}
			
			else if(!shoppingResultView.getPurchasedItems().isEmpty() && !shoppingResultView.getDroppedItems().isEmpty()) {
				shoppingResultView.setResult(Result.PARTIAL);
			}
			
			else if(shoppingResultView.getPurchasedItems().isEmpty() && shoppingResultView.getDroppedItems().isEmpty()) {
				shoppingResultView.setResult(Result.EMPTY);
			}
			
			shopResults.add(shoppingResultView);
			
			synchronized (lockShopResults) {
				for(ShoppingResultView srv : this.shopHistory()) {
					if(srv.getId().equals(shoppingResultView.getId())) {
						lastRegisteredShopResults.add(shoppingResultView);
						
						try {
							SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
								
							System.out.println("Generating random byte array ...");
									
							final byte array[] = new byte[32];
							random.nextBytes(array);
							
							shopResultsUpdateNonce = Base64.getEncoder().encodeToString(array);
						} catch (NoSuchAlgorithmException e) {
							System.out.println("No such algorithm found for generating the nonce");
						}
					}
				}
			}
			lastRegisteredShopNonce = propertyValue;
		}
		else {
			System.out.println("Pedido já recebido da ultima vez");
		}
		return shoppingResultView;
	}    
	
	
	// Auxiliary operations --------------------------------------------------
	@Override
	public String ping(String name) {
		if (name == null || name.trim().length() == 0)
			name = "friend";
		
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		
		System.out.println("Ping");
		try {
			String results = "";
			Collection<String> namingList = uddiNaming.list("T14_Supplier%");
			for(String string : namingList) {
				if(string == null || string == "" || string.trim() == "")
					break;
				
				System.out.println(string);
				System.out.println(namingList.size());
                SupplierClient supplierClient = new SupplierClient(string);
                results = supplierClient.ping(name) + ";\n" + results;
			}
			return results;
		} catch (UDDINamingException e) {
			System.out.println("Error on dealing with UDDI: " + e);
		} catch (SupplierClientException e) {
			System.out.println("Error on dealing with Supplier Client: " + e);
		}
		
		return null;
	}

	@Override
	public void clear() {
		Mediator.getInstance().reset();
		
		UDDINaming uddiNaming = this.endpointManager.getUddiNaming();
		
		try {
		Collection<String> wsURLList = uddiNaming.list("T14_Supplier%");

			for(String wsURL : wsURLList) {
				SupplierClient supplierClient = new SupplierClient(wsURL);
				supplierClient.clear();
			}
		} catch (SupplierClientException e) {
			System.out.println("Error on dealing with UDDI: " + e);
		} catch (UDDINamingException e) {
			System.out.println("Error on dealing with Supplier Client: " + e);
		}
		
	}

	@Override
	public List<CartView> listCarts() {
		Mediator mediator = Mediator.getInstance();
		List<CartView> cvs = new ArrayList<CartView>();

		for(String cid : mediator.getCartIDs()) {
			Cart cart = mediator.getCart(cid);
			CartView cartView = new CartView();
			cartView.setCartId(cid);
			
			for(Item item : cart.getList()) {
				ItemIdView itemIdView = newItemIdView(item); 
				ItemView itemView = newItemView(itemIdView, item);
				CartItemView cartItemView = newCartItemView(itemView, item);

				
				cartView.getItems().add(cartItemView);
			}
			
			cvs.add(cartView);
		}
		return cvs;
	}
	
	@Override
	public List<ShoppingResultView> shopHistory() {

		Comparator<ShoppingResultView> comparator = new Comparator<ShoppingResultView>() {
			@Override
			public int compare(ShoppingResultView srv_1, ShoppingResultView srv_2) {
				return Integer.compare(Integer.parseInt(srv_1.getId()), Integer.parseInt(srv_2.getId()));
			}
		};
		
		Collections.sort(shopResults, comparator);
				
		return shopResults;
	}
	
	
	// Replication operations --------------------------------------------------
	@Override
	public void imAlive() {
		if (this.endpointManager.getWsName() != null) {
		}
		else {
			primaryOnline = true;
			
			Date actualDate = new Date();
			lastRegisteredTimestamp = dateFormatter.format(actualDate);
		}
	}
	
	@Override
	public void updateShopHistory(List<ShoppingResultView> shopResults, String nonce) {
		boolean exists = false;
		
		if(!nonce.equals(lastRegisteredShopNonce)) {
			for(ShoppingResultView recv_srv : shopResults) {
				System.out.println(recv_srv.getId());
				System.out.println(recv_srv.getTotalPrice());
				for(ShoppingResultView actual_srv : this.shopResults) {
					if (actual_srv.getId().equals(recv_srv.getId())) {
						exists = true;
					}
				}
				if (!exists) {
					this.shopHistory().add(recv_srv);
				}
				
				else {
					exists = false;
				}
			}
		}
		
		else {
			System.out.printf("Nonce: %s%n", lastRegisteredNonce);
			System.out.println("Already received!");
		}

		System.out.println("################");
		System.out.println("Actual");
		System.out.println("################");
		for(ShoppingResultView shoppingResultView : this.shopHistory()) {
			System.out.println(shoppingResultView.getId());
			System.out.println(shoppingResultView.getTotalPrice());
		}
		
	}

	@Override
	public void updateCart(List<CartView> updatedCarts, String nonce) {
		Mediator mediator = Mediator.getInstance();
		
		if(!nonce.equals(lastRegisteredNonce)) {
			System.out.println(updatedCarts.size());
			for(CartView cartView : updatedCarts) {
				CartItemView cartItemView = cartView.getItems().get(0);
				
				ItemView itemView = cartItemView.getItem();
				ItemIdView itemIdView = itemView.getItemId();
					
				String productId = itemIdView.getProductId();
				String supplierId = itemIdView.getSupplierId();
					
				String desc = itemView.getDesc();
				
				int price = itemView.getPrice();
				int itemQty = cartItemView.getQuantity();
					
				mediator.addToCart(cartView.getCartId(), productId , supplierId, price, desc, itemQty);
					
				lastRegisteredNonce = nonce;
			}
		}
		
		else {
			System.out.printf("Nonce: %s%n", lastRegisteredNonce);
			System.out.println("Already received!");
		} 
		
		
		System.out.println("entra aqui");
		for(CartView cartView : listCarts()) {
			for(CartItemView cartItemView : cartView.getItems()) {
				ItemView itemView = cartItemView.getItem();
				ItemIdView itemIdView = itemView.getItemId();
				System.out.println("");
				System.out.println("#######################");
				System.out.print("CartId: ");
				System.out.println(cartView.getCartId());
				String productId = itemIdView.getProductId();
				String supplierId = itemIdView.getSupplierId();
				System.out.print("ProductId: ");
				System.out.println(productId);
				String desc = itemView.getDesc();
				System.out.print("SupplierId: ");
				System.out.println(supplierId);
				System.out.print("Description: ");
				System.out.println(desc);
				int price = itemView.getPrice();
				int itemQty = cartItemView.getQuantity();
				System.out.print("Quantidade: ");
				System.out.println(itemQty);
				System.out.print("Price: ");
				System.out.println(price);
				System.out.println("#######################");
				System.out.println("");
			}
		} 
	}
	
	
	// View helpers -----------------------------------------------------
	private ItemIdView newItemIdView(Item item) {
		ItemIdView itemIdView = new ItemIdView();
		itemIdView.setProductId(item.getProductId());
		itemIdView.setSupplierId(item.getSupplierID());
		return itemIdView;
	}
	
	private ItemView newItemView(ItemIdView itemIdView, Item item) {
		ItemView itemView = new ItemView();
		itemView.setDesc(item.getDesc());
		itemView.setItemId(itemIdView);
		itemView.setPrice(item.getPrice());
		return itemView;
	}	

	private CartItemView newCartItemView(ItemView itemView, Item item) {
		CartItemView cartItemView = new CartItemView();
		cartItemView.setItem(itemView);
		cartItemView.setQuantity(item.getQuantity());
		return cartItemView;
	}

	
	// Exception helpers -----------------------------------------------------

	private void throwInvalidItemId(final String message) throws InvalidItemId_Exception {
		InvalidItemId faultInfo = new InvalidItemId();
		faultInfo.setMessage(message);
		throw new InvalidItemId_Exception(message, faultInfo);
	}

	private void throwInvalidText(final String message) throws InvalidText_Exception {
		InvalidText faultInfo = new InvalidText();
		faultInfo.setMessage(message);
		throw new InvalidText_Exception(message, faultInfo);
	}
	
	private void throwNotEnoughItems(final String message) throws NotEnoughItems_Exception {
		NotEnoughItems faultInfo = new NotEnoughItems();
		faultInfo.setMessage(message);
		throw new NotEnoughItems_Exception(message, faultInfo);
	}
	
	private void throwInvalidQuantity(final String message) throws InvalidQuantity_Exception {
		InvalidQuantity faultInfo = new InvalidQuantity();
		faultInfo.setMessage(message);
		throw new InvalidQuantity_Exception(message, faultInfo);
	}
	
	private void throwEmptyCart(final String message) throws EmptyCart_Exception {
		EmptyCart faultInfo = new EmptyCart();
		faultInfo.setMessage(message);
		throw new EmptyCart_Exception(message, faultInfo);
	}
	
	private void throwInvalidCartId(final String message) throws InvalidCartId_Exception {
		InvalidCartId faultInfo = new InvalidCartId();
		faultInfo.setMessage(message);
		throw new InvalidCartId_Exception(message, faultInfo);
	}
	
	private void throwInvalidCreditCard(final String message) throws InvalidCreditCard_Exception {
		InvalidCreditCard faultInfo = new InvalidCreditCard();
		faultInfo.setMessage(message);
		throw new InvalidCreditCard_Exception(message, faultInfo);
	}

}
