package com.rate.limit.service.controller;

import com.rate.limit.service.dto.RateLimitResponse;
import com.rate.limit.service.service.RateLimiterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rate-limiter-service/")
public class RateLimiterController {
    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping
    public RateLimitResponse callRateLimiterAlgo(){
      return rateLimiterService.tokenBucketAlgorithm(LocalDateTime.now());
    }
}
