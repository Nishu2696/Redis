package com.example.__spring_redis_distrubuted_locks.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DistrubutedLockService {
    private final StringRedisTemplate stringRedisTemplate;

//    Duration.ofSeconds(30) - >
//                              This is needed, because once a product goes into a lock mode and after that app crashes,
//                              it shouldn't forever be in a lock state, that's why a TTL
//                              Now with this a drawback of SETNX,

//    say we set our TTL have been set 30 seconds, but our logic [db operation, connecting with another microservice and reverting back takes more than 30 secs],
//    then this lock will get free and another user can utilize this lock,
//    we eventually end up on concurrent lock which will cause an issue in production,
//    to resolve this,
//    we move to Redisson,
//    where a release lock is not dependent on pre-defined duration,
//    rather it depends on when the current logic execution gets completed, then only the lock gets free.

    public boolean acquireLock(String lockKey, String ownerId) {
        Boolean result = stringRedisTemplate
                .opsForValue()
                .setIfAbsent(
                        lockKey,
                        ownerId,
                        Duration.ofSeconds(30) // This is needed, because once a product goes into a lock mode and after that app crashes, it shouldn't forever be in a lock state, thats why a TTL
                );

        return Boolean.TRUE.equals(result);
    }

    public void releaseLock(String lockKey, String ownerId) {
        String currentOwnerId = stringRedisTemplate
                .opsForValue()
                .get(lockKey);
        if (currentOwnerId.equals(ownerId)) {
            stringRedisTemplate.delete(lockKey);
        }
    }
}
