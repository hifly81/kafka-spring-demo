package io.confluent.springboot.kafka.demo.producer;

import io.confluent.springboot.kafka.demo.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class Producer {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Value("${spring.kafka.topic.name}")
    private String TOPIC;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrder(Order order) {
        logger.info(String.format("#### -> Producing message -> %s", order));
        this.kafkaTemplate.send(TOPIC, String.valueOf(order.getId()), order.getId() + "-" + order.getName());
    }

}