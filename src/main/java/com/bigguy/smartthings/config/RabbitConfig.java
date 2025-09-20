package com.bigguy.smartthings.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RxabbitConfig {

    @Bean
    public Queue smartthingsQueue() {
        return new Queue("smartthings-events", true);
    }
}
