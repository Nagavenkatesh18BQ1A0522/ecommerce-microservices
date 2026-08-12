package com.shop.inventory.stock;

import com.shop.inventory.stock.dto.InventoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
}
