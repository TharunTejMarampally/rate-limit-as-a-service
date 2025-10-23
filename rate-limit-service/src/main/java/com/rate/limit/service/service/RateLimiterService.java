package com.rate.limit.service.service;

import com.lib.common_lib.dto.RateLimitResponse;
import com.lib.common_lib.entity.AlgorithmState;
import com.rate.limit.service.repository.RateLimiterRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static com.lib.common_lib.constants.RateLimitAsAServiceConstants.FREE;


@Service
public class RateLimiterService {


    private final RateLimiterRepository rateLimiterRepository;
    private final KafkaProducerService kafkaProducerService;
    private static final String KAFKA_TOPIC ="state";

    private final ExecutorService freeExecutorService;
    private final ExecutorService paidExecutorService;

    public RateLimiterService(RateLimiterRepository rateLimiterRepository, KafkaProducerService kafkaProducerService) {
        this.rateLimiterRepository = rateLimiterRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.freeExecutorService = new ThreadPoolExecutor(
                10,
                20,
                60,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.paidExecutorService= new ThreadPoolExecutor(
                50,
                100,
                60,
                TimeUnit.SECONDS,new LinkedBlockingDeque<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public CompletableFuture<RateLimitResponse> tokenBucketAlgorithmAsync(String planType,LocalDateTime currentTime){
        if(planType.equals(FREE)){
            return CompletableFuture.supplyAsync(() -> tokenBucketAlgorithm(currentTime), freeExecutorService);
        }else{
            return CompletableFuture.supplyAsync(() -> tokenBucketAlgorithm(currentTime), paidExecutorService);
        }
    }

    public RateLimitResponse tokenBucketAlgorithm(LocalDateTime currentTime){
        int refillRate=2;
        int refillTimeIntervalSeconds=5;
        int maxBucketCapacity=10;
        int currentTokens;
        LocalDateTime lastUpdatedTime;
        AlgorithmState previousState=rateLimiterRepository.retrieveLastInsertedValue();

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
        AlgorithmState algorithmState=new AlgorithmState();
        algorithmState.setCurrentTokens(currentTokens);
        algorithmState.setRefileRate(refillRate);
        algorithmState.setMaxBucketSize(maxBucketCapacity);
        algorithmState.setTimeStamp(currentTime);
        algorithmState.setAllowed(allowed);
        rateLimiterRepository.save(algorithmState);

        CompletableFuture.runAsync(() ->
                kafkaProducerService.sendUser(KAFKA_TOPIC, algorithmState)
        );
        return new RateLimitResponse(currentTokens,allowed,LocalDateTime.now());
    }

    @PreDestroy
    public void shutDownFreeThreadPool(){
        freeExecutorService.shutdown();
        paidExecutorService.shutdown();
    }
}
