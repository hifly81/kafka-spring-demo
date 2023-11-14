package io.confluent.springboot.kafka.demo.controller;

import io.confluent.springboot.kafka.demo.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.confluent.springboot.kafka.demo.model.Order;

@RestController
public class KafkaController {

   private final Producer producer;

   @Autowired
   KafkaController(Producer producer) {
       this.producer = producer;
   }

    @PostMapping(value="/api/order")
    public ResponseEntity send(@RequestBody Order order) {
        this.producer.sendOrder(order);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value="/api/v2/order")
    public ResponseEntity sendV2(@RequestBody Order order) {
        this.producer.sendOrderV2(order);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value="/api/v2/order/npe")
    public ResponseEntity sendOrderV2WithNPE(@RequestBody Order order) {
        this.producer.sendOrderV2WithNPE(order);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}