package com.shop.orders.order;

public class InsufficientStock extends RuntimeException{
    public InsufficientStock(String productCode, int requested, int available) {
        super("Insufficient stock for " + productCode + ": requested " + requested + ", available " + available);
    }
}
