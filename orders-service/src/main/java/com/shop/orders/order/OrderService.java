package com.shop.orders.order;

import com.shop.orders.order.dto.CreateOrderRequest;
import com.shop.orders.order.dto.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;


    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(request.customerId(), request.productCode(), request.quantity(), request.amount());
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(), order.getCustomerId(), order.getProductCode(),
                order.getQuantity(), order.getAmount(), order.getStatus(), order.getCreatedAt());
    }
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFound(id));
        return toResponse(order);
    }
}
