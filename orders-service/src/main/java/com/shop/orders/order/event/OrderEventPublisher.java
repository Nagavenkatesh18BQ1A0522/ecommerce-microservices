package com.shop.orders.order.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})   // Boot's auto-configured KafkaTemplate is generically <?,?>
public class OrderEventPublisher {
    public static final String ORDER_PLACED_TOPIC = "orders.order-placed";

    private final KafkaTemplate kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlaced event) {
        kafkaTemplate.send(ORDER_PLACED_TOPIC, String.valueOf(event.orderId()), event);
    }
}
