package com.rate.limit.service.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    private static final String REDIS_HOST = "full-stag-35077.upstash.io";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_USERNAME = "default";
    private static final String REDIS_PASSWORD = "AYkFAAIncDE0YTNiNzc1NGVhYzc0Y2U2OTcyNDJhNTdmY2JlY2YwY3AxMzUwNzc";

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(REDIS_HOST, REDIS_PORT);
        redisConfig.setUsername(REDIS_USERNAME);
        redisConfig.setPassword(REDIS_PASSWORD);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(60))
                .useSsl()
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer serializer=new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisTemplate<String,Object> template=new RedisTemplate<>();

        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(serializer);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

}
