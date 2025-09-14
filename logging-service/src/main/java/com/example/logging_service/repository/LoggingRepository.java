package com.example.logging_service.repository;

import com.lib.common_lib.entity.RateLimitLog;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LoggingRepository extends ElasticsearchRepository<RateLimitLog,String> {
}
