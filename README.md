# Overview

# Pre Requisites

 - Spring Boot Version: 2.7.1.7. Latest available for 2.x
 - JDK 11
 - Spring Kafka: 2.8.11. This version ships Apache Kafka 3.0.2. There is no version available for Apache Kafka 3.1, version shipped in Confluent Platform 7.1

# How to run on local

```bash
#start a producer on port 8010
cd kafka-springboot-producer
mvn spring-boot:run

#start a consumer on port 8090
cd kafka-springboot-consumer
mvn spring-boot:run

# Publish orders: topic orders
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order

# Publish orders, simulating an ERROR: topic orders
curl --data '{"id":5, "name": "ERROR-PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order
----
```