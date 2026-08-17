package com.shop.inventory.stock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    InventoryItemRepository repository;

    @InjectMocks
    InventoryService inventoryService;

    @Test
    void reserveStock_reducesQuantity_whenEnoughStock() {
        InventoryItem item = new InventoryItem("BOOK-123", 50);
        when(repository.findByProductCode("BOOK-123")).thenReturn(Optional.of(item));

        inventoryService.reserveStock("BOOK-123", 5);

        assertThat(item.getAvailableQuantity()).isEqualTo(45);   // 50 - 5
    }
}