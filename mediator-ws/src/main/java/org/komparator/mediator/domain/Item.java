package org.komparator.mediator.domain;

public class Item {
	
	private final String productId;  /* final provavelmente */
	private final String supplierId;  /* final provavelmente */
	private String desc;
	private int price;
	private int quantity = 0;
	
	public Item(String productId, String supplierId, int price, String desc, int quantity) { /* talvez dar so os dois final */
		this.productId = productId;
		this.supplierId = supplierId;
		this.desc = desc;
		this.price = price;
		this.quantity = quantity;
	}

	public String getProductId() {
		return this.productId;
	}

	public String getSupplierID() {
		return this.supplierId;
	}
	
	public int getPrice() {
		return this.price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public String getDesc() {
		return this.desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/** Synchronized locks object before returning quantity */
	public synchronized int getQuantity() {
		return this.quantity;
	}

	/** Synchronized locks object before setting quantity */
	public synchronized void setQuantity(int quantity) {
		this.quantity = quantity;
	}


}
