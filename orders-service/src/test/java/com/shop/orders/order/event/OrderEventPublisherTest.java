package com.shop.orders.order.event;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { OrderEventPublisher.ORDER_PLACED_TOPIC })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class OrderEventPublisherTest {

    @Autowired
    OrderEventPublisher publisher;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    @Test
    void publishOrderPlaced_sendsEventToTopic() {
        // a raw string consumer subscribed to the topic (reads the JSON as text)
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                consumerProps("test-group", "true", embeddedKafka),
                new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, OrderEventPublisher.ORDER_PLACED_TOPIC);

        // act
        publisher.publishOrderPlaced(new OrderPlaced(1L, 1L, "BOOK-123", 2, new BigDecimal("49.99")));

        // assert the event landed on the topic
        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, OrderEventPublisher.ORDER_PLACED_TOPIC, Duration.ofSeconds(10));
        assertThat(record.key()).isEqualTo("1");                 // key = orderId
        assertThat(record.value()).contains("BOOK-123");         // JSON payload has the product
        consumer.close();
    }
}