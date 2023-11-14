package io.confluent.springboot.kafka.demo.handler;

import org.springframework.stereotype.Service;

@Service
public class ProcessHandler {

    public void generateException(String message) {
        throw new RuntimeException(message);
    }

    public void execute(String message) {
        System.out.println("echo:" + message);
    }
}
