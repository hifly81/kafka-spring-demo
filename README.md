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
 - Add ACLs for user _alice_


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

# Error Handling and Retries on consumers

## Blocking Retries: DefaultErrorHandler

The default behavior is attempting to consume one massage at most 10 times. After that the consumer will never process it again.
Reference class is: _org.springframework.kafka.listener.DefaultErrorHandler_.

This implementation overrides the default behaviour setting a max number for retries to 2 with a _backoff interval_ of 15 seconds.
_NPE_ will not be retried.

 1. Place order on topic _orders_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order
```

 2. Verify consuming on consumer log and retries exhausted:

```bash
2023-11-14 11:53:37.071  INFO 57032 --- [ntainer#0-0-C-1] i.c.s.k.d.c.ConsumerWithDefaultRetries   : #### -> Consumed message -> ConsumerRecord(topic = orders, partition = 0, leaderEpoch = 0, offset = 1, CreateTime = 1699959216868, serialized key size = 1, serialized value size = 10, headers = RecordHeaders(headers = [], isReadOnly = false), key = 5, value = {"name": "PS5", "id": 5})
2023-11-14 11:53:52.081  INFO 57032 --- [ntainer#0-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group-3, groupId=order-app-group] Seeking to offset 1 for partition orders-0
2023-11-14 11:53:52.085 ERROR 57032 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Error handler threw an exception

2023-11-14 11:53:52.090  INFO 57032 --- [ntainer#0-0-C-1] i.c.s.k.d.c.ConsumerWithDefaultRetries   : #### -> Consumed message -> ConsumerRecord(topic = orders, partition = 0, leaderEpoch = 0, offset = 1, CreateTime = 1699959216868, serialized key size = 1, serialized value size = 10, headers = RecordHeaders(headers = [], isReadOnly = false), key = 5, value = {"name": "PS5", "id": 5})
2023-11-14 11:54:07.189  INFO 57032 --- [ntainer#0-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group-3, groupId=order-app-group] Seeking to offset 1 for partition orders-0
2023-11-14 11:54:07.192 ERROR 57032 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Error handler threw an exception
```

## Non-Blocking Retries and Dead Letter Topics

Based on the work at:
https://github.com/eugene-khyst/spring-kafka-non-blocking-retries-and-dlt

1. Place order on topic _orders_v2_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/v2/order
```

2. Verify consuming on consumer log and retries on _orders_v2-retry-0, orders_v2-retry-1, orders_v2-retry-2 and orders_v2-dlt_ topics:

```bash
2023-11-14 00:48:14.973  INFO 31012 --- [1-retry-0-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group-6, groupId=order-app-group] Seeking to offset 1 for partition orders_v2-retry-0-0

2023-11-14 00:48:16.046  INFO 31012 --- [2-retry-1-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group-5, groupId=order-app-group] Seeking to offset 1 for partition orders_v2-retry-1-0

2023-11-14 00:48:18.129  INFO 31012 --- [3-retry-2-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group-3, groupId=order-app-group] Seeking to offset 1 for partition orders_v2-retry-2-0

2023-11-14 00:48:21.687 ERROR 31012 --- [3-retry-2-0-C-1] k.r.DeadLetterPublishingRecovererFactory : Record: topic = orders_v2-retry-2, partition = 0, offset = 1, main topic = orders_v2 threw an error at topic orders_v2-retry-2 and won't be retried. Sending to DLT with name orders_v2-dlt.
```

### NPE not retriable and sent to DLT

This configuration will not retry messages for _NPE_ but those will be sent directly to _DLT_.

1. Place order on topic _orders_v2_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/v2/order/npe
```

2. Verify consuming on consumer log and on orders_v2-dlt_ topics:

```bash
2023-11-14 11:07:50.071 ERROR 55406 --- [ntainer#0-0-C-1] k.r.DeadLetterPublishingRecovererFactory : Record: topic = orders_v2, partition = 0, offset = 5, main topic = orders_v2 threw an error at topic orders_v2 and won't be retried. Sending to DLT with name orders_v2-dlt.
```
