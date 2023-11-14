package io.confluent.springboot.kafka.demo.producer;

import io.confluent.springboot.kafka.demo.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Producer {

    @Value("${spring.kafka.topic.name}")
    private String TOPIC;

    @Value("${spring.kafka.topic.name}" + "_v2")
    private String TOPIC_V2;

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    public Producer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        this.kafkaTemplate.send(this.TOPIC, String.valueOf(order.getId()), order);
    }

    public void sendOrderV2(Order order) {
        this.kafkaTemplate.send(this.TOPIC_V2, String.valueOf(order.getId()), order);
    }
}