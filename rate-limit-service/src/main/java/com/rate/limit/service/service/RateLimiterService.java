package com.rate.limit.service.service;

import com.rate.limit.service.entity.AlgorithmState;
import com.rate.limit.service.repository.RateLimiterRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RateLimiterService {

    private final RateLimiterRepository rateLimiterRepository;

    public RateLimiterService(RateLimiterRepository rateLimiterRepository) {
        this.rateLimiterRepository = rateLimiterRepository;
    }

    public String tokenBucketAlgorithm(LocalDateTime currentTime){
        //varibles initialization
        String response=null;
        int refilRate=2;
        int maxBucketCapacity=10;
        int currentTokens=0;

        LocalDateTime lastUpdatedTime= LocalDateTime.now();
        //before caluculatiing the timediff call the redis for the last updated time
        long timeDifference=Duration.between(currentTime,lastUpdatedTime).toSeconds();


        currentTokens= (int) Math.min(maxBucketCapacity,currentTokens + timeDifference*refilRate);
        //save these details into redis maxBucketSize,currentTokens in this state, currentTime, refilRate
        AlgorithmState algorithmState=new AlgorithmState();
        algorithmState.setCurrentTokens(currentTokens);
        algorithmState.setRefileRate(refilRate);
        algorithmState.setMaxBucketSize(maxBucketCapacity);
        rateLimiterRepository.save(algorithmState);

        if(currentTokens<1){
            response="No tokens";
        }else{
            response="Tokens available";
        }
        return response;
    }

}
