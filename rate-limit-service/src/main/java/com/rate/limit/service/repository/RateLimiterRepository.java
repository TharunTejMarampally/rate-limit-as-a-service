package com.rate.limit.service.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rate.limit.service.entity.AlgorithmState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class RateLimiterRepository {

    private static final String KEY="STATE";

    private final RedisTemplate<String,Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimiterRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    //save
    public void save(AlgorithmState algorithmState){
        redisTemplate.opsForHash().put(KEY,algorithmState.getId(),algorithmState);
        redisTemplate.opsForList().rightPush(KEY+"_IDS",algorithmState.getId());
    }

    //retrive
    public AlgorithmState retrive(String id){
        return (AlgorithmState) redisTemplate.opsForHash().get(KEY,id);
    }

    //get the last inserted json
    public AlgorithmState retriveLastInsertedValue(){
        String id= (String) redisTemplate.opsForList().index(KEY+"_IDS",-1);
        if(StringUtils.hasText(id)){
            Object raw=redisTemplate.opsForHash().get(KEY,id);
            return objectMapper.convertValue(raw,AlgorithmState.class);
        }
        return null;
    }
}
