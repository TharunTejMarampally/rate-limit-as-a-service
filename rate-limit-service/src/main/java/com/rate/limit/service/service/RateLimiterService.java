package com.rate.limit.service.service;

import com.rate.limit.service.dto.RateLimitResponse;
import com.rate.limit.service.entity.AlgorithmState;
import com.rate.limit.service.repository.RateLimiterRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RateLimiterService {

    private final RateLimiterRepository rateLimiterRepository;

    public RateLimiterService(RateLimiterRepository rateLimiterRepository) {
        this.rateLimiterRepository = rateLimiterRepository;
    }

    public RateLimitResponse tokenBucketAlgorithm(LocalDateTime currentTime){
        int refilRate=2;
        int maxBucketCapacity=10;

        AlgorithmState previousState=rateLimiterRepository.retriveLastInsertedValue();

        int currentTokens;
        LocalDateTime lastUpdatedTime;

        if(previousState!=null&& previousState.getTimeStamp() != null){
           lastUpdatedTime=previousState.getTimeStamp();
           currentTokens=previousState.getCurrentTokens();
        }else{
            lastUpdatedTime=LocalDateTime.now();
            currentTokens=maxBucketCapacity;
        }

        long timeDifference=Duration.between(lastUpdatedTime, currentTime).toSeconds();


        currentTokens= (int) Math.min(maxBucketCapacity,currentTokens + timeDifference*refilRate);
        boolean allowed;
        if(currentTokens<1){
          allowed=false;
        }else{
            currentTokens -= 1;
            allowed=true;
        }
        //save these details into redis maxBucketSize,currentTokens in this state, currentTime, refilRate
        AlgorithmState algorithmState=new AlgorithmState();
        algorithmState.setCurrentTokens(currentTokens);
        algorithmState.setRefileRate(refilRate);
        algorithmState.setMaxBucketSize(maxBucketCapacity);
        algorithmState.setTimeStamp(LocalDateTime.now());
        rateLimiterRepository.save(algorithmState);
        return new RateLimitResponse(currentTokens,allowed,LocalDateTime.now());
    }

}
