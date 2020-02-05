package org.komparator.mediator.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Mediator {
		
		private ConcurrentHashMap<String, Cart> carts = new ConcurrentHashMap<String, Cart>();

		
		private Mediator() {
		
		}

		private static class SingletonHolder {
			private static final Mediator INSTANCE = new Mediator();
		}

		public static synchronized Mediator getInstance() {
			return SingletonHolder.INSTANCE;
		}

		// product ---------------------------------------------------------------

		public void reset() {
			carts.clear();
		}

		public Boolean cartExists(String cartId) {
			return carts.containsKey(cartId);
		}

		public Set<String> getCartIDs() {
			return carts.keySet();
		}
		
		public Cart getCart(String cartId) {
			return carts.get(cartId);
		}
		
		public void addCart(String cartId, Cart cart){
			carts.put(cartId, cart);
		}
		
		public synchronized void addToCart(String cartId, String productId, String supplierId, int price, String desc, int itemQty) {
			if(this.cartExists(cartId)) {
				Cart cart = this.carts.get(cartId);
				
				Item item = cart.getItem(productId, supplierId);
				
				if(item == null) {
					System.out.println("nao existe");
					Item newItem = new Item(productId, supplierId, price, desc, itemQty);
					cart.addItem(newItem);
				}
				
				else {
					System.out.println("ja existe");
					int q = item.getQuantity() + itemQty;
					item.setQuantity(q);
				}
				
			}
			
			
			else {
				Item item = new Item(productId, supplierId, price, desc, itemQty);
				Cart cart = new Cart(cartId, item);
				
				addCart(cartId, cart);
				System.out.println("passou3.5");
			}
		}
		
}