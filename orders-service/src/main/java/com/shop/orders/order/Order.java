package com.shop.orders.order;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long customerId;
    private String productCode;
    private	int quantity;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private	OrderStatus status;
    private Instant createdAt;

    protected Order() {
    }

    public Order( long customerId,String productCode, int quantity, BigDecimal amount) {
        this.amount = amount;
        this.quantity = quantity;
        this.productCode = productCode;
        this.customerId = customerId;
        status = OrderStatus.PLACED;
        createdAt= Instant.now();

    }
}
