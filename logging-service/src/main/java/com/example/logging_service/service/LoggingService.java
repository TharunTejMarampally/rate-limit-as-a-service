package com.example.logging_service.service;

import com.example.logging_service.repository.LoggingRepository;
import com.lib.common_lib.entity.AlgorithmState;
import com.lib.common_lib.entity.RateLimitLog;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class LoggingService {

    private final LoggingRepository loggingRepository;

    public LoggingService(LoggingRepository loggingRepository) {
        this.loggingRepository = loggingRepository;
    }

    @KafkaListener(topics = "state", groupId = "state-consumer-group-logging")
    public void consumeMessage(AlgorithmState message) throws IllegalAccessException {

        if(message == null){
            throw new IllegalAccessException("Kafka Response is Empty");
        }
        List<RateLimitLog> newLogs = new ArrayList<>();
            RateLimitLog rateLimitLog=new RateLimitLog();
            rateLimitLog.setTimeStamp(LocalDate.from(message.getTimeStamp()));
            rateLimitLog.setCurrentTokens(message.getCurrentTokens());
            rateLimitLog.setAllowed(message.isAllowed());
            rateLimitLog.setMaxBucketSize(message.getMaxBucketSize());
            rateLimitLog.setRefileRate(message.getRefileRate());
            newLogs.add(rateLimitLog);

        loggingRepository.saveAll(newLogs);
    }

    public List<RateLimitLog> getKafkaMessages(){
        return StreamSupport.stream(loggingRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

}
