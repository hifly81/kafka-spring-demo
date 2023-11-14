# Overview

# Pre Requisites

 - Spring Boot Version: 2.7.1.7. Latest available for 2.x
 - Spring Kafka: 2.8.11. This version ships Apache Kafka 3.1.2, version shipped in Confluent Platform 7.1.1.
 - JDK 11

To run CP demo:

 - Docker engine version _20.10.12_
 - Docker Compose version _2.4.1_
 - JDK 11 version _11.0.8_

CP demo will start up:

 - broker listening on port _9092_ with authorizer based on ACLs
 - schema registry listening on port _8081_
 - ACLs added for user _alice/alice-secret_


# How to run on local

## Start CP demo

```bash
cp/start.sh
```

## Start Demo Application

### Producer

Producer configurations with default values for CP 7.1 are visible at:
https://docs.confluent.io/platform/7.1/installation/configuration/producer-configs.html

```bash
cd kafka-springboot-producer
mvn spring-boot:run
```

### Consumers

Consumer configurations with default values for CP 7.1 are visible at:
https://docs.confluent.io/platform/7.1/installation/configuration/consumer-configs.html

```bash
cd kafka-springboot-consumer
mvn spring-boot:run

2023-11-14 12:31:08.348  INFO 58556 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : order-app-group: partitions assigned: [orders-0]
2023-11-14 12:31:08.348  INFO 58556 --- [ntainer#1-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : order-app-group_v2: partitions assigned: [orders_v2-0]
```

## Teardown

```bash
cp/stop.sh
```

# Error Handling and Retries on producers

Important configurations for error handling and retries on producing. Suggestion is to maintain the default value.

 - _retries_: resend any record whose send fails with a potentially transient error. Note that this retry is no different than if the client resent the record upon receiving the error. Produce requests will be failed before the number of retries has been exhausted if the timeout configured by _delivery.timeout.ms_ expires first before successful acknowledgement. Users should generally prefer to leave this config unset and instead use _delivery.timeout.ms_ to control retry behavior. 
 **Default:	2147483647**


 - _delivery.timeout.ms_: An upper bound on the time to report success or failure after a call to _send()_ returns. This limits the total time that a record will be delayed prior to sending, the time to await acknowledgement from the broker (if expected), and the time allowed for retriable send failures. The producer may report failure to send a record earlier than this config if either an unrecoverable error is encountered, the retries have been exhausted, or the record is added to a batch which reached an earlier delivery expiration deadline. The value of this config should be greater than or equal to the sum of _request.timeout.ms_ and _linger.ms_.
  **Default:	120000 (2 minutes)**

 - _retry.backoff.ms_: The amount of time to wait before attempting to retry a failed request to a given topic partition. This avoids repeatedly sending requests in a tight loop under some failure scenarios.
  **Default:	100**


 - _enable.idempotence_: the producer will ensure that exactly one copy of each message is written in the stream. Note that enabling idempotence requires _max.in.flight.requests.per.connection_ to be less than or equal to 5 (with message ordering preserved for any allowable value), _retries_ to be greater than 0, and _acks_ must be 'all'.
 **Default:	true**


 - _max.in.flight.requests.per.connection_
   The maximum number of unacknowledged requests the client will send on a single connection before blocking
   **Default:	5**

## Non Retriable Errors: Callback

This implementation (class _Producer_) provides a callback (_onFailure_ method) that reacts on
non retriable errors or if _retries_ are been exhausted.

1. Place order on topic _orders_ko_ (_TopicNotFound_ or _TopicAuthorizationException_) and simulate an error:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order/ko
```

```bash
org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [orders_ko]

2023-11-14 15:09:06.593 ERROR 66091 --- [nio-8010-exec-1] i.c.s.kafka.demo.producer.Producer       : unable to send order: {"name": "PS5", "id": 5}

org.springframework.kafka.KafkaException: Send failed; nested exception is org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [orders_ko]
at org.springframework.kafka.core.KafkaTemplate.doSend(KafkaTemplate.java:666) ~[spring-kafka-2.8.11.jar:2.8.11]
at org.springframework.kafka.core.KafkaTemplate.send(KafkaTemplate.java:409) ~[spring-kafka-2.8.11.jar:2.8.11]
```

# Error Handling and Retries on consumers

## Blocking Retries: DefaultErrorHandler

The default behavior is attempting to consume one massage at most 10 times. After that the consumer will never process it again. Retries **are not handled in a non-blocking mode**.
Reference class is: _org.springframework.kafka.listener.DefaultErrorHandler_.

This implementation (class _ConsumerWithDefaultRetries_) overrides the default behaviour setting a max number for retries to 2 with a _backoff interval_ of 15 seconds.
_NPE_ will not be retried.

 1. Place order on topic _orders_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order
```

 2. Verify consuming on consumer log and retries exhausting:

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

This implementation (class _ConsumerWithDLQ_) configure 3 retriable topics, _orders_v2-retry-0_ _(backoff 1 seconds)_, _orders_v2-retry-1_ _(backoff 2 seconds)_, _orders_v2-retry-2_ _(backoff 4 seconds)_ and a _DLT_ topic, _orders_v2-dlt_, for the main topic _orders_v2_.

Offending messages will be retried **without blocking** consuming of messages.
After exhausting the retries, messages will be sent to _DLT_.

1. Place order on topic _orders_v2_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/v2/order
```

2. Verify consuming on consumer log and retries on _orders_v2-retry-0, orders_v2-retry-1, orders_v2-retry-2 and orders_v2-dlt_ topics:

```bash
2023-11-14 12:38:22.244  INFO 59124 --- [2-retry-0-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group_v2-retry-0-4, groupId=order-app-group_v2-retry-0] Seeking to offset 0 for partition orders_v2-retry-0-0
2023-11-14 12:38:23.289  INFO 59124 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Resetting the last seen epoch of partition orders_v2-retry-1-0 to 0 since the associated topicId changed from null to QYj_jdSYTQyayBWSt9tfaA
2023-11-14 12:38:23.341  INFO 59124 --- [3-retry-1-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group_v2-retry-1-2, groupId=order-app-group_v2-retry-1] Seeking to offset 0 for partition orders_v2-retry-1-0
2023-11-14 12:38:25.399  INFO 59124 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Resetting the last seen epoch of partition orders_v2-retry-2-0 to 0 since the associated topicId changed from null to 6guENInWSgWk09WC-PeEQg
2023-11-14 12:38:25.460  INFO 59124 --- [4-retry-2-0-C-1] o.a.k.clients.consumer.KafkaConsumer     : [Consumer clientId=consumer-order-app-group_v2-retry-2-6, groupId=order-app-group_v2-retry-2] Seeking to offset 0 for partition orders_v2-retry-2-0
2023-11-14 12:38:28.919 ERROR 59124 --- [4-retry-2-0-C-1] k.r.DeadLetterPublishingRecovererFactory : Record: topic = orders_v2-retry-2, partition = 0, offset = 0, main topic = orders_v2 threw an error at topic orders_v2-retry-2 and won't be retried. Sending to DLT with name orders_v2-dlt.
2023-11-14 12:38:29.448  INFO 59124 --- [ad | producer-1] org.apache.kafka.clients.Metadata        : [Producer clientId=producer-1] Resetting the last seen epoch of partition orders_v2-dlt-0 to 0 since the associated topicId changed from null to st_aTG2JRT-kNAGwwHUY9A
2023-11-14 12:38:29.495  INFO 59124 --- [ner#5-dlt-0-C-1] i.c.s.k.demo.consumer.ConsumerWithDLQ    : ConsumerRecord(topic = orders_v2-dlt, partition = 0, leaderEpoch = 0, offset = 0, CreateTime = 1699961909449, serialized key size = 1, serialized value size = 10,
```

### NPE not retriable and sent to DLT

This configuration will not retry messages for _NPE_ but those will be sent directly to _DLT_.

1. Place order on topic _orders_v2_:

```bash
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/v2/order/npe
```

2. Verify consuming on consumer log and on _orders_v2-dlt_ topics:

```bash
2023-11-14 12:40:37.350  INFO 59124 --- [ntainer#1-0-C-1] i.c.s.k.demo.consumer.ConsumerWithDLQ    : ConsumerRecord(topic = orders_v2, partition = 0, leaderEpoch = 0, offset = 1, CreateTime = 1699962037313, serialized key size = 1, serialized value size = 10, headers = RecordHeaders(headers = [RecordHeader(key = X-Custom-Header, value = [78, 80, 69])], isReadOnly = false), key = 5, value = {"name": "PS5", "id": 5}) from orders_v2
2023-11-14 12:40:37.351 ERROR 59124 --- [ntainer#1-0-C-1] k.r.DeadLetterPublishingRecovererFactory : Record: topic = orders_v2, partition = 0, offset = 1, main topic = orders_v2 threw an error at topic orders_v2 and won't be retried. Sending to DLT with name orders_v2-dlt.
2023-11-14 12:40:37.902  INFO 59124 --- [ner#5-dlt-0-C-1] i.c.s.k.demo.consumer.ConsumerWithDLQ    : ConsumerRecord(topic = orders_v2-dlt, partition = 0, leaderEpoch = 0, offset = 1, CreateTime = 1699962037887, serialized key size = 1, serialized value size = 10, headers = RecordHeaders(headers = [RecordHeader(key = X-Custom-Header
```
