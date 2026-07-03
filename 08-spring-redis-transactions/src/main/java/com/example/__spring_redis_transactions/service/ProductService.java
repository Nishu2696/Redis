package com.example.__spring_redis_transactions.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    public final RedisTemplate<String, String> redisTemplate;

    public void updateStock() {
        redisTemplate.execute(
                new SessionCallback<>() {

                    @Override
                    public Object execute(
                            RedisOperations operations
                    ) {

                        operations.watch("stock");

                        operations.multi();

                        operations.opsForValue()
                                .decrement("stock");

                        return operations.exec();
                    }
                });
    }
}

//            updateStock()
//
//                │
//
//            WATCH stock
//
//                │
//
//            MULTI
//
//                │
//
//            DECR stock
//
//                │
//
//            EXEC
//
//                ▼
//
//            Commit If Not Modified
