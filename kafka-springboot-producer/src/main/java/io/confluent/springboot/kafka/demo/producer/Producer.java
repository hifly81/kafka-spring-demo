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

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    public Producer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        this.kafkaTemplate.send(this.TOPIC, String.valueOf(order.getId()), order);
    }
}