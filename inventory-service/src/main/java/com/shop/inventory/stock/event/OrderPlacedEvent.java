package com.shop.inventory.stock.event;

import java.math.BigDecimal;

public record OrderPlacedEvent(Long orderId,
                               long customerId,
                               String productCode,
                               int quantity,
                               BigDecimal amount) {
}
