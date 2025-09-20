package com.bigguy.smartthings;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Service
public class QueueListener {

    @RabbitListener(queues = "smartthings-events")
    public void processMessage(String message) {
        System.out.println("Received from RabbitMQ: " + message);
    }
}
