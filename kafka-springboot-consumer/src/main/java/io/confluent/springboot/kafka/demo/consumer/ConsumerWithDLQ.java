package io.confluent.springboot.kafka.demo.consumer;

import io.confluent.springboot.kafka.demo.handler.ProcessHandler;
import io.confluent.springboot.kafka.demo.model.Order;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;


@Component
public class ConsumerWithDLQ {

    @Autowired
    ProcessHandler processHandler;

    @Value(value = "${spring.kafka.topic.name}" + "_v2")
    private String topic;

    private final Logger logger = LoggerFactory.getLogger(ConsumerWithDLQ.class);

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "${spring.kafka.topic.name}" + "_v2")
    public void listen(ConsumerRecord<String, Order> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info(record + " from " + topic);
        processHandler.generateException("I'm an Exception v2");
    }

    @DltHandler
    public void dlt(ConsumerRecord<String, Order> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        logger.info(record + " from " + topic);
    }

}

