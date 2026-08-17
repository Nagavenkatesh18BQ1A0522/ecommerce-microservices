package com.shop.inventory.stock.event;


import com.shop.inventory.stock.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryEventListener {

    public static final String ORDER_PLACED_TOPIC = "orders.order-placed";

    private final InventoryService inventoryService;

    public InventoryEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = ORDER_PLACED_TOPIC, groupId = "inventory-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlaced for order {} ({} x {})",
                event.orderId(), event.quantity(), event.productCode());
        inventoryService.reserveStock(event.productCode(), event.quantity());
    }
}
