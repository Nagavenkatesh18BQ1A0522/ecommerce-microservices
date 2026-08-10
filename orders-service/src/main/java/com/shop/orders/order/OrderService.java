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
    public OrderResponse createOrder(CreateOrderRequest request){
        Order order = new Order(request.customerId(), request.productCode(), request.quantity(), request.amount());
        Order saved =   orderRepository.save(order);
        return new OrderResponse(saved.getId(), saved.getCustomerId(), saved.getProductCode(), saved.getQuantity(), saved.getAmount(),saved.getStatus(),saved.getCreatedAt());

    }
}
