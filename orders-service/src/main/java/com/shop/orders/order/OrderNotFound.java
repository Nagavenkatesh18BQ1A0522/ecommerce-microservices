package com.shop.orders.order;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(Long id) {
        super("Order not found with id: " + id);
    }
}
