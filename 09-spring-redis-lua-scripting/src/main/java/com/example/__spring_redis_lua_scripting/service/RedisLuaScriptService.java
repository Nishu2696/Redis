package com.example.__spring_redis_lua_scripting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RedisLuaScriptService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> rateLimiterScript;

    public boolean allowRequest(String userId) {
        String key = "rate_limit:" + userId;
        long currentCount = redisTemplate.execute(
                rateLimiterScript,
                Collections.singletonList(key),
                60
        );
        return currentCount <= 100;
    }
}

//Collections.singletonList(key),
//Why List<String> keys? Because redis supports of passing multiple keys to LUA Script