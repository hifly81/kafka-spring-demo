package io.confluent.springboot.kafka.demo.consumer;

import io.confluent.springboot.kafka.demo.handler.ProcessHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import io.confluent.springboot.kafka.demo.model.Order;


@Service
public class ConsumerWithDefaultRetries {

    @Autowired
    ProcessHandler processHandler;

    @Value(value = "${spring.kafka.topic.name}")
    private String topic;

    private final Logger logger = LoggerFactory.getLogger(ConsumerWithDefaultRetries.class);

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, Order> record) {
        logger.info(String.format("#### -> Consumed message -> %s", record));
        //generate an exception
        processHandler.generateException("I'm an Exception");
    }

}