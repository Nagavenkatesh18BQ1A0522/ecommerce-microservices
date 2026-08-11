package com.shop.orders.order;

import com.shop.orders.order.dto.CreateOrderRequest;
import com.shop.orders.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;

    @Test
    void createOrder_returns201_whenRequestIsValid() throws Exception {
        OrderResponse stub = new OrderResponse(
                1L, 1L, "BOOK-123", 2, new BigDecimal("49.99"),
                OrderStatus.PLACED, Instant.parse("2026-01-01T00:00:00Z"));
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(stub);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":1,"productCode":"BOOK-123","quantity":2,"amount":49.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productCode").value("BOOK-123"))
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(header().string("Location", "/orders/1"));
    }
    @Test
    void createOrder_returns400_whenRequestIsInvalid() throws Exception {

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":1,"productCode":"","quantity":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.productCode").exists())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.quantity").exists()
                );
    }

    @Test
    void getOrder_returns200_whenOrderExists() throws Exception{
        OrderResponse stub = new OrderResponse(
                1L, 1L, "BOOK-123", 2, new BigDecimal("49.99"),
                OrderStatus.PLACED, Instant.parse("2026-01-01T00:00:00Z"));
        when(orderService.getOrder(1L)).thenReturn(stub);
        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PLACED"));
    }

    @Test
    void getOrder_returns404_whenOrderNotFound() throws Exception{

       
        when(orderService.getOrder(999L)).thenThrow(new OrderNotFound(999L));
        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Order not found"));
    }
}
