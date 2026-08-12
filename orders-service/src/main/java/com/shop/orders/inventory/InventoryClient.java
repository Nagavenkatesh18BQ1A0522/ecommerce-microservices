package com.shop.orders.inventory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {
    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {   // ← no Builder param
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();        // ← static RestClient.builder()
    }

    public InventoryView getStock(String productCode) {
        return restClient.get()
                .uri("/inventory/{productCode}", productCode)
                .retrieve()
                .body(InventoryView.class);
    }
}
