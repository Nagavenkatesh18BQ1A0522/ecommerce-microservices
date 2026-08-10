package com.shop.orders.order.dto;

import com.shop.orders.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        long customerId,
        String productCode,
        int quantity,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt
) {}
