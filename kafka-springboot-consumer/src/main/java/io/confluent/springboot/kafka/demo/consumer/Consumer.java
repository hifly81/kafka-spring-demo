package io.confluent.springboot.kafka.demo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.group-id}")
    public void consume(String message) {
        logger.info(String.format("#### -> Consumed message -> %s", message));
    }
}