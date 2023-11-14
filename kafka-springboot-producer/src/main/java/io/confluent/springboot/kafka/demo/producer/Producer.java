package io.confluent.springboot.kafka.demo.producer;

import io.confluent.springboot.kafka.demo.model.Order;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayList;
import java.util.List;

@Service
public class Producer {

    @Value("${spring.kafka.topic.name}")
    private String TOPIC;

    @Value("${spring.kafka.topic.name}" + "_v2")
    private String TOPIC_V2;

    private final KafkaTemplate<String, Order> kafkaTemplate;

    private final Logger logger = LoggerFactory.getLogger(Producer.class);

    @Autowired
    public Producer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrder(Order order) {
        this.kafkaTemplate.send(this.TOPIC, String.valueOf(order.getId()), order);
    }

    public void sendOrderKO(Order order) {
        try {
            ListenableFuture<SendResult<String, Order>> future = this.kafkaTemplate.send(this.TOPIC + "_ko", String.valueOf(order.getId()), order);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(final SendResult<String, Order> result) {
                    logger.info("sent order: " + order + " with offset: " + result.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    logger.error("onFailure --> unable to send order: " + order, throwable);
                }
            });
        } catch(Exception ex) {
            logger.error("unable to send order: " + order, ex);
        }

    }

    public void sendOrderV2(Order order) {
        this.kafkaTemplate.send(this.TOPIC_V2, String.valueOf(order.getId()), order);
    }

    public void sendOrderV2WithNPE(Order order) {
        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader("X-Custom-Header", "NPE".getBytes()));
        ProducerRecord<String, Order> orderRecord = new ProducerRecord<>(this.TOPIC_V2, 0, String.valueOf(order.getId()), order, headers);
        this.kafkaTemplate.send(orderRecord);
    }
}