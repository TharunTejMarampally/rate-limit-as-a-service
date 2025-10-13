package com.rate.limit.service.service;

import com.lib.common_lib.dto.RateLimitResponse;
import com.lib.common_lib.entity.AlgorithmState;
import com.rate.limit.service.repository.RateLimiterRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

@Service
public class RateLimiterService {

    private final RateLimiterRepository rateLimiterRepository;
    private final KafkaProducerService kafkaProducerService;
    private static final String KAFKA_TOPIC ="state";

    private final ExecutorService executorService;

    public RateLimiterService(RateLimiterRepository rateLimiterRepository, KafkaProducerService kafkaProducerService) {
        this.rateLimiterRepository = rateLimiterRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.executorService = new ThreadPoolExecutor(
                10,
                20,
                60,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public CompletableFuture<RateLimitResponse> tokenBucketAlgorithmAsync(LocalDateTime currentTime){
        return CompletableFuture.supplyAsync(() -> tokenBucketAlgorithm(currentTime),executorService);
    }

    public RateLimitResponse tokenBucketAlgorithm(LocalDateTime currentTime){
        int refillRate=2;
        int refillTimeIntervalSeconds=5;
        int maxBucketCapacity=10;
        int currentTokens;
        LocalDateTime lastUpdatedTime;
        AlgorithmState previousState=rateLimiterRepository.retriveLastInsertedValue();

        if(previousState!=null&& previousState.getTimeStamp() != null){
           lastUpdatedTime=previousState.getTimeStamp();
           currentTokens=previousState.getCurrentTokens();
        }else{
            lastUpdatedTime=LocalDateTime.now();
            currentTokens=maxBucketCapacity;
        }
        long timeDifference = Duration.between(lastUpdatedTime, currentTime).toSeconds();
        int extraTokensToAdd = (int) (((double) timeDifference / refillTimeIntervalSeconds) * refillRate);
        currentTokens = Math.min(maxBucketCapacity, currentTokens + extraTokensToAdd);
        boolean allowed;
        if(currentTokens<1){
          allowed=false;
        }else{
            currentTokens -= 1;
            allowed=true;
        }
        //save these details into redis maxBucketSize,currentTokens in this state, currentTime, refillRate
        AlgorithmState algorithmState=new AlgorithmState();
        algorithmState.setCurrentTokens(currentTokens);
        algorithmState.setRefileRate(refillRate);
        algorithmState.setMaxBucketSize(maxBucketCapacity);
        algorithmState.setTimeStamp(currentTime);
        algorithmState.setAllowed(allowed);
        rateLimiterRepository.save(algorithmState);

        kafkaProducerService.sendUser(KAFKA_TOPIC, algorithmState);

        return new RateLimitResponse(currentTokens,allowed,LocalDateTime.now());
    }

    @PreDestroy
    public void shutDownThreadPool(){
        executorService.shutdown();
    }

}
