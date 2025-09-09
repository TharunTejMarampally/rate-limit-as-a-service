package com.example.logging_service.repository;

import com.example.logging_service.entity.RateLimitLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LoggingRepository extends ElasticsearchRepository<RateLimitLog,String> {
}
