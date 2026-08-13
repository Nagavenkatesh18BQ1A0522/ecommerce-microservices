package com.shop.orders.inventory;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.base-url}") String baseUrl) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))          // fail fast if we can't even connect
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(2));          // fail fast if the response is too slow
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "getStockFallback")
    @Retry(name = "inventory")
    public InventoryView getStock(String productCode) {
        return restClient.get()
                .uri("/inventory/{productCode}", productCode)
                .retrieve()
                .body(InventoryView.class);
    }

    // Called when the circuit is open OR all retries are exhausted.
    // Note the extra Throwable parameter — that's how Resilience4j matches a fallback.
    private InventoryView getStockFallback(String productCode, Throwable t) {
        throw new InventoryUnavailable(productCode, t);
    }
}