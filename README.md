# Overview

# Pre Requisites

 - Spring Boot Version: 2.7.1.7. Latest available for 2.x
 - JDK 11
 - Spring Kafka: 2.8.11. This version ships Apache Kafka 3.1.2, version shipped in Confluent Platform 7.1.1

To run CP demo:

 - Docker engine version 20.10.12
 - Docker Compose version 2.4.1
 - Java version 11.0.8

CP demo will start up:

 - broker listening on port 9092 with ACL
 - schema registry listening on port 8081
 - Read and Write permission on topic orders for user "alice"
 - Read permission on group order-app-group for user "alice"


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

# Publish orders: topic orders
curl --data '{"id":5, "name": "PS5"}' -H "Content-Type:application/json" http://localhost:8010/api/order
```

## Teardown

```bash
cp/stop.sh
```