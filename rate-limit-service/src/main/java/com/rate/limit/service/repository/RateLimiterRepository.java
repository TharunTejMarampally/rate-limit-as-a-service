package com.rate.limit.service.repository;

import com.rate.limit.service.entity.AlgorithmState;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class RateLimiterRepository {

    private static final String KEY="STATE";

    private final RedisTemplate<String,Object> redisTemplate;

    public RateLimiterRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
    public AlgorithmState retriveLastInsertedjson(){
        String id= (String) redisTemplate.opsForList().index(KEY+"_IDS",-1);
        if(StringUtils.hasText(id)){
            return (AlgorithmState) redisTemplate.opsForHash().get(KEY,id);
        }
        return null;
    }
}
