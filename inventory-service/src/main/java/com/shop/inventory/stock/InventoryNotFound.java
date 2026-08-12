package com.shop.inventory.stock;

public class InventoryNotFound extends RuntimeException {
    public InventoryNotFound(String productCode) {
        super("No inventory for product " + productCode);
    }
}
