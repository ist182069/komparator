package org.komparator.mediator.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart entity.
 */
public class Cart {
	
	/** Cart identifier. */
	private final String cartId; /* final provavelmente */
	
	/** Product description. */
	private List<Item> itemList = new ArrayList<Item>();
	
	/** Create a new product */ 
	public Cart(String cartId, Item item) { /* Possivelmente deveria excluir o constructor de ter null e passar a usar addItem */
		this.cartId = cartId;
		this.itemList.add(item); //cart pode ser criado sem item
	}

	public String getId() {
		return cartId;
	}

	public List<Item> getList() {
		return itemList;
	}
	
	public void addItem(Item item) {
		this.itemList.add(item);
	}
	
	public Item getItem(String productId, String supplierId) {
		for(Item item : itemList) {
			if(item.getProductId().equals(productId) && item.getSupplierID().equals(supplierId)) {
				return item;
			}
		}
		return null;
	}

}
