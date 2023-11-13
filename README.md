# Overview

# Pre Requisites

 - Spring Boot Version: 2.7.1.7. Latest available for 2.x
 - JDK 11
 - Spring Kafka: 2.8.11. This version ships Apache Kafka 3.1.2, version shipped in Confluent Platform 7.1.1

To run CP demo:

 - Docker engine version _20.10.12_
 - Docker Compose version _2.4.1_
 - Java version _11.0.8_

CP demo will start up:

 - broker listening on port _9092_ with ACL
 - schema registry listening on port _8081_
 - Read and Write permission on topic _orders_ for user _alice_
 - Read permission on group _order-app-group_ for user _alice_


# How to run on local

## Start CP demo version 7.1.1

```bash
cp/start.sh
```

## Start Demo Application

```bash
#start a producer on port 8010
cd kafka-springboot-producer
mvn spring-boot:run

#start a consumer on port 8090
cd kafka-springboot-consumer
mvn spring-boot:run
```

## Teardown

```bash
cp/stop.sh
```

# Test Scenarios

## Produce and Consume

 1. Publish orders on topic _orders_

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order
```

 2. Verify consuming on consumer log

```bash
2023-11-13 09:31:02.453  INFO 4865 --- [ntainer#0-0-C-1] i.c.s.kafka.demo.consumer.Consumer       : #### -> Consumed message -> ConsumerRecord(topic = orders, partition = 0, leaderEpoch = 0, offset = 0, CreateTime = 1699864261965, serialized key size = 1, serialized value size = 10, headers = RecordHeaders(headers = [], isReadOnly = false), key = 5, value = {"name": "PS5", "id": 5})
```