package com.rate.limit.service.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lib.common_lib.entity.AlgorithmState;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
public class RateLimiterRepository {

    private static final String KEY = "STATE";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimiterRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(AlgorithmState algorithmState) {
        String id = algorithmState.getId();

        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                int retries = 3;
                while (retries-- > 0) {
                    operations.watch(KEY);
                    operations.multi();
                    operations.opsForHash().put(KEY, id, algorithmState);
                    operations.opsForList().rightPush(KEY + "_IDS", id);

                    List<Object> execResult = operations.exec();
                    if (!CollectionUtils.isEmpty(execResult)) {
                        return true;
                    }
                    operations.unwatch();
                }
                throw new IllegalStateException("Failed to save state after retries");
            }
        });
    }
    public AlgorithmState retrieveLastInsertedValue() {
        String id = (String) redisTemplate.opsForList().index(KEY + "_IDS", -1);
        if (!StringUtils.hasText(id)) {
            return null;
        }
        Object raw = redisTemplate.opsForHash().get(KEY, id);
        return raw != null ? objectMapper.convertValue(raw, AlgorithmState.class) : null;
    }

}
