package com.shop.orders.inventory;

public class InventoryUnavailable extends RuntimeException {
    public InventoryUnavailable(String productCode, Throwable cause) {
        super("Inventory service unavailable, cannot verify stock for " + productCode, cause);
    }
}
