package com.example.logging_service.controller;

import com.example.logging_service.service.LoggingService;
import com.lib.common_lib.entity.RateLimitLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logging-service")
public class LoggingServiceController {
    private final LoggingService loggingService;

    public LoggingServiceController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping("/messages")
    List<RateLimitLog> getKafakMessage(){
        return loggingService.getKafkaMessages();
    }
}
