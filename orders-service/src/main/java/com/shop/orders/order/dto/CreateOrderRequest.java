package com.shop.orders.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(@NotNull Long customerId, @NotBlank String productCode, @Positive int quantity, @NotNull @Positive
                                 BigDecimal amount) {
}
