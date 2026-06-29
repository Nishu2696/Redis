package com.example.spring_redis_serialization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}

/*
* If we don't add line 14-18, while getting the object we will get random objects [\xac\xed\x00\x05sr...]
* Reason being to store the data in Redis we need it in JSON format, but the data available is in Java Object
* Hence a serializer is being used while setting the data
* And at the fetch place we are de-serializing it back
*
* NOTE: Here we have used RedisTemplate ---> This is used for objects
*       If we had only Strings to be stored in Redis at that time instead of using Redis Template we will be using StringRedisTemplate
            * public StringRedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
                    StringRedisTemplate<String, String> redisTemplate = new StringRedisTemplate<>();
                    redisTemplate.setConnectionFactory(redisConnectionFactory);
                    redisTemplate.setKeySerializer(new StringRedisSerializer()); // Convert all the redis key into plain string
                    redisTemplate.setHashKeySerializer(new StringRedisSerializer());
                    redisTemplate.setHashValueSerializer(RedisSerializer.json());
                    redisTemplate.setValueSerializer(RedisSerializer.json()); // Convert Java Object into JSON and then stores it in Redis
                    redisTemplate.afterPropertiesSet();
                    return redisTemplate;
                }
* */
