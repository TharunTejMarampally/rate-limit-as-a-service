package com.rate.limit.service.service;

import com.lib.common_lib.dto.RateLimitResponse;
import com.lib.common_lib.entity.AlgorithmState;
import com.rate.limit.service.custonException.PreviousStateNotFound;
import com.rate.limit.service.repository.RateLimiterRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static com.lib.common_lib.constants.RateLimitAsAServiceConstants.ERROR_MESSAGE;
import static com.lib.common_lib.constants.RateLimitAsAServiceConstants.FREE;


@Service
public class RateLimiterService {


    private final RateLimiterRepository rateLimiterRepository;
    private final KafkaProducerService kafkaProducerService;
    private static final String KAFKA_TOPIC ="state";

    private ExecutorService freeExecutorService;
    private ExecutorService paidExecutorService;

    @Value("${app.refillRate}")
    private int refillRate;

    @Value("${app.refillTimeIntervalSeconds}")
    private int refillTimeIntervalSeconds;

    @Value("${app.maxBucketCapacity}")
    private int maxBucketCapacity;

    @Value("${app.isNotificationToKafka}")
    private boolean isNotificationToKafka;

    @Value("${app.freePlan.corePoolSize}")
    private int freeCorePoolSize;

    @Value("${app.freePlan.maxPoolSize}")
    private int freeMaxPoolSize;

    @Value("${app.freePlan.keepAliveTime}")
    private int freeKeepAliveTime;

    @Value("${app.freePlan.dequeSize}")
    private int freeDequeSize;

    @Value("${app.paidPlan.corePoolSize}")
    private int paidCorePoolSize;

    @Value("${app.paidPlan.maxPoolSize}")
    private int paidMaxPoolSize;

    @Value("${app.paidPlan.keepAliveTime}")
    private int paidKeepAliveTime;

    @Value("${app.paidPlan.dequeSize}")
    private int paidDequeSize;

    public RateLimiterService(RateLimiterRepository rateLimiterRepository,
                              KafkaProducerService kafkaProducerService) {
        this.rateLimiterRepository = rateLimiterRepository;
        this.kafkaProducerService = kafkaProducerService;
    }
    @PostConstruct
    private void initExecutors() {
        this.freeExecutorService = new ThreadPoolExecutor(
                freeCorePoolSize,
                freeMaxPoolSize,
                freeKeepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(freeDequeSize),
                new ThreadPoolExecutor.CallerRunsPolicy());

        this.paidExecutorService = new ThreadPoolExecutor(
                paidCorePoolSize,
                paidMaxPoolSize,
                paidKeepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(paidDequeSize),
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
        int currentTokens;
        LocalDateTime lastUpdatedTime;
        AlgorithmState previousState=rateLimiterRepository.retrieveLastInsertedValue();
//        if(previousState==null){
//            throw new PreviousStateNotFound(ERROR_MESSAGE);
//        }
        if(previousState!=null && previousState.getTimeStamp() != null){
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

        if(isNotificationToKafka){
            CompletableFuture.runAsync(() ->
                    kafkaProducerService.sendUser(KAFKA_TOPIC, algorithmState)
            );
        }
        return new RateLimitResponse(currentTokens,allowed,LocalDateTime.now());
    }

    @PreDestroy
    public void shutDownFreeThreadPool(){
        freeExecutorService.shutdown();
        paidExecutorService.shutdown();
    }
}
