package com.shop.orders.order.event;

import java.math.BigDecimal;

public record OrderPlaced(Long orderId,
                          long customerId,
                          String productCode,
                          int quantity,
                          BigDecimal amount) {
}
