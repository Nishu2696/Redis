package com.example.__spring_redis_ttl_expiration.service;

import com.example.__spring_redis_ttl_expiration.dto.User;
import com.example.__spring_redis_ttl_expiration.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class UserCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofSeconds(60);

    public void cachedUser(User user) {
        String key = "user:" + user.getId();
        redisTemplate.opsForValue().set(key, user, TTL);
    }

    public User getUser(String id) {
        String key = "user:" + id;
        User user = (User) redisTemplate.opsForValue().get(key);
        if (user != null) {
            refreshTTl(key);
        }
        return user;
    }

    public void refreshTTl(String key) {
        redisTemplate.expire(key, TTL); // A new TTL is being set
    }
}
