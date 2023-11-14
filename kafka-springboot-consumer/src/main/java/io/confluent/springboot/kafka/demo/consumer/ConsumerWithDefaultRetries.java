package io.confluent.springboot.kafka.demo.consumer;

import io.confluent.springboot.kafka.demo.handler.ProcessHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;

import io.confluent.springboot.kafka.demo.model.Order;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;


@Service
public class ConsumerWithDefaultRetries {

    @Autowired
    ProcessHandler processHandler;

    @Value(value = "${spring.kafka.topic.name}")
    private String topic;

    @Value(value = "${spring.kafka.consumer.backoff-interval}")
    private Long interval;

    @Value(value = "${spring.kafka.consumer.backoff-max_failure}")
    private Long maxAttempts;

    private final Logger logger = LoggerFactory.getLogger(ConsumerWithDefaultRetries.class);

    @Bean
    public DefaultErrorHandler errorHandler() {
        BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
        }, fixedBackOff);
        errorHandler.addNotRetryableExceptions(NullPointerException.class);
        return errorHandler;
    }

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, Order> record) {
        logger.info(String.format("#### -> Consumed message -> %s", record));
        //generate an exception
        processHandler.generateException("I'm an Exception");
    }

}