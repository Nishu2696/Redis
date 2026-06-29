package com.example.spring_redis.service;

import com.example.spring_redis.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveUser(User user) {
        redisTemplate.opsForValue().set("User" + user.getId(), user.getName());
    }

    public User getUser(String id) {
        return (User) redisTemplate.opsForValue().get("User" + id);
    }
}
