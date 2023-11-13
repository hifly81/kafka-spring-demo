package io.confluent.springboot.kafka.demo.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import io.confluent.springboot.kafka.demo.model.Order;
import org.springframework.util.backoff.FixedBackOff;

@Service
public class Consumer {

    @Value(value = "${spring.kafka.topic.name}")
    private String topic;

    private final Logger logger = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.group-id}")
    public void consume(ConsumerRecord<String, Order> record) {
        logger.info(String.format("#### -> Consumed message -> %s", record));
    }

    @Bean(name = "eh")
    public ErrorHandler eh(KafkaOperations<String, String> template) {
        return new SeekToCurrentErrorHandler(
          new DeadLetterPublishingRecoverer(template,
                  (rec, ex) -> {
                      return new TopicPartition("DLT."+topic, rec.partition());
                  }), new FixedBackOff(0L, 0L));
    }

    @KafkaListener(id = "listener-1", topics = "${spring.kafka.topic.name}")
    public void listen(ConsumerRecord<String, Order> record,
                       @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("Record value: {} @ offset: {}", record.value(), offset);
        throw new IllegalStateException("failed");
    }

    @KafkaListener(id = "listener-1.DLT", topics = "DLT."+"${spring.kafka.topic.name}")
    public void listenDLT(ConsumerRecord<String, Order> record,
                          @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("DLT record value: {} @ offset: {}", record.value(), offset);
    }
}