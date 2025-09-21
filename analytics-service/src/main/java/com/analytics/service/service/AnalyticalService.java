package com.analytics.service.service;


import com.lib.common_lib.entity.AlgorithmState;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class AnalyticalService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticalService.class);

    private final Counter consumedCounter;
    private final Counter allowedRequests;
    private final Counter blockedRequests;

    public AnalyticalService(MeterRegistry meterRegistry) {
        this.consumedCounter = Counter.builder("kafka_messages_consumed_total")
                .description("Total messages consumed from Kafka")
                .register(meterRegistry);
        this.allowedRequests = Counter.builder("rate_limit_requests_allowed_total")
                .description("Total allowed requests by rate limiter")
                .register(meterRegistry);
        this.blockedRequests = Counter.builder("rate_limit_requests_blocked_total")
                .description("Total blocked requests by rate limiter")
                .register(meterRegistry);
    }

    @KafkaListener(
            topics = "state",
            groupId = "state-consumer-group-analytics"
    )
    public void consumeMessage(
            @Payload AlgorithmState message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            if (message == null) {
                logger.error("Received null message from Kafka");
                return;
            }
            consumedCounter.increment();
            if (message.isAllowed()) {
                allowedRequests.increment();
            } else {
                blockedRequests.increment();
            }

        } catch (Exception e) {
            logger.error("Error processing Kafka message: ", e);
        }
    }

    public String getCounters() {
        return String.format("Consumed: %.0f, Allowed: %.0f, Blocked: %.0f",
                consumedCounter.count(),
                allowedRequests.count(),
                blockedRequests.count());
    }

}
