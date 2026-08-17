package com.shop.inventory.stock;

import com.shop.inventory.stock.dto.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryService(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    private InventoryResponse toResponse(InventoryItem inventoryItem) {
        return new InventoryResponse(
                inventoryItem.getProductCode(),
                inventoryItem.getAvailableQuantity());
    }

    @Transactional(readOnly = true)
    public  InventoryResponse getByProductCode(String productcode){
       InventoryItem inventoryItem =  inventoryItemRepository.findByProductCode(productcode)
               .orElseThrow(() -> new InventoryNotFound(productcode));
       return toResponse(inventoryItem);
    }

    @Transactional
    public void reserveStock(String productCode, int quantity) {
        inventoryItemRepository.findByProductCode(productCode).ifPresentOrElse(item -> {
            if (item.getAvailableQuantity() >= quantity) {
                item.reduceStock(quantity);                       // dirty-checking saves it (inside @Transactional)
                log.info("Reserved {} of {}", quantity, productCode);
            } else {
                log.warn("Insufficient stock for {} (need {}, have {})", productCode, quantity, item.getAvailableQuantity());
            }
        }, () -> log.warn("No inventory row for product {}", productCode));
    }
}
