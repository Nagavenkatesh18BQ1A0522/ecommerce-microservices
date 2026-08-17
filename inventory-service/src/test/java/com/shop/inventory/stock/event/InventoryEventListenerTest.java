package com.shop.inventory.stock.event;

import com.shop.inventory.stock.InventoryService;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = { InventoryEventListener.ORDER_PLACED_TOPIC })
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class InventoryEventListenerTest {

    @Autowired
    EmbeddedKafkaBroker broker;

    @MockitoBean
    InventoryService inventoryService;   // mock it — we're testing the listener, not the DB

    @Test
    void onOrderPlaced_dispatchesToReserveStock() {
        // publish a raw JSON OrderPlaced onto the topic
        Producer<String, String> producer = new DefaultKafkaProducerFactory<>(
                KafkaTestUtils.producerProps(broker),
                new StringSerializer(), new StringSerializer()).createProducer();
        String json = "{\"orderId\":1,\"customerId\":1,\"productCode\":\"BOOK-123\",\"quantity\":5,\"amount\":49.99}";
        producer.send(new ProducerRecord<>(InventoryEventListener.ORDER_PLACED_TOPIC, "1", json));
        producer.flush();
        producer.close();

        // the listener runs on a background thread → wait, then verify it dispatched
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                verify(inventoryService).reserveStock("BOOK-123", 5));
    }
}