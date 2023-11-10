package io.confluent.springboot.kafka.demo.consumer;

import io.confluent.springboot.kafka.demo.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class Receiver {

    Logger logger = LoggerFactory.getLogger(Receiver.class);

    @KafkaListener(topics = "${topic-name}")
    public void listen(@Payload String message) {

        logger.info("received message:" + message);

        Order order = new Order();

        //generate random id
        order.setId(ThreadLocalRandom.current().nextLong(10000000));
        order.setName(message);

        if (order.getName().contains("ERROR-"))
            throw new OrderException();


    }


}