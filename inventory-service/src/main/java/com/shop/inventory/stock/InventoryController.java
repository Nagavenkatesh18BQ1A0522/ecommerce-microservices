package com.shop.inventory.stock;

import com.shop.inventory.stock.dto.InventoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private  final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productCode}")
    public ResponseEntity<InventoryResponse> getByProductCode(@PathVariable String productCode){
        InventoryResponse response = inventoryService.getByProductCode(productCode);
        return ResponseEntity.ok(response);

    }
}
