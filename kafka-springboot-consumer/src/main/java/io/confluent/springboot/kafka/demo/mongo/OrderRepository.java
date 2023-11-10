package io.confluent.springboot.kafka.demo.mongo;

import io.confluent.springboot.kafka.demo.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
 

public interface OrderRepository extends MongoRepository<Order, Long> {

}