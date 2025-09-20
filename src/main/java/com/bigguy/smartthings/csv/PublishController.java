package com.bigguy.smartthings.csv;

import com.bigguy.smartthings.model.PublishRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publish")
public class PublishController {

    private final RabbitTemplate rabbitTemplate;

    public PublishController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public String publishMessage(@RequestBody PublishRequest request) {
        String payload;

        if (request.getContent() != null && !request.getContent().isEmpty()) {
            payload = "CSV Content: " + request.getContent();
        } else if (request.getFilename() != null) {
            payload = "CSV File Received: " + request.getFilename();
        } else {
            payload = "CSV File Received (no details)";
        }

        rabbitTemplate.convertAndSend("smartthings-events", payload);
        return "✅ Published to RabbitMQ: " + payload;
    }
}
