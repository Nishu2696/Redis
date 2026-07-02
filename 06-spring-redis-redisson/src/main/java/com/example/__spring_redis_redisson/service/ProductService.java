package com.example.__spring_redis_redisson.service;

import com.example.__spring_redis_redisson.entity.ProductEntity;
import com.example.__spring_redis_redisson.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private ProductRepository productRepository;
    private final RedissonClient redissonClient;

//    Note here we have used lock.lock()
//    which means a watch dog will be created which will internally set a TTL
//    internally inside redisson there is a file called "internalLockLeaseTime" which has default time set as 30s, so for every second this watch dog will be monitoring
//    after 30 s if the lock is not freed up it will extend the ttl again by 30s
//    so in this way dynamic TTL is set

//    what if the app crashes at 20sec, then in this case watchdog dies, and after 30 seconds the lock gets free, because that is the default TTL

//    Now what if I need to write a custom TTL
//    NOTE: we can create a custom TTL, but this breaks the property of redisson, which is basically, the lock release should be dynamic

//    but still if we want to achieve the above requirement where we define our TTL in our redisson
//    then the syntax looks like this: lock.lock(60, TimeUnit.seconds) here i am mentioning the lock should be of 60sec and not default 30 sec
//    Once we define a TTL, then in that case, there will be no watchdog associated for this lock, and it will behave like a SETNX, what we discussed previously

    @Transactional
    public void purchase(Long productId){
        String lockName = "product:" +  productId;
        RLock lock = redissonClient.getLock(lockName);
        lock.lock();
        try {
            ProductEntity product = productRepository.findById(productId).orElseThrow();

            if (product.getStock() <= 0) {
                throw new RuntimeException("Product out of stock");
            }

            product.setStock(product.getStock() - 1);
            productRepository.save(product);
        } finally {
            lock.unlock();
        }
    }

//    Note here we have used RLock lock = redissonClient.getLock(lockName);

//    RedisTemplate Gives
    //    opsForValue()
    //    opsForHash()
    //    opsForSet()
    //    opsForList()

//    Redisson Gives
    //    getLock()
    //    getMap()
    //    getSet()
    //    getQueue()
    //    getSemaphore()
    //    getReadWriteLock()

//    similarly we have used lock.lock()
//    the drawback with this is, what if the the task that is running in the background takes longer time or it gets crashed
//    then in that case our watch dog will be monitoring continuosly and the lock will never be released

//          App-1
//            │
//            ▼
//          Owns Lock
//     ----------------
//          App-2
//            │
//            ▼
//          Waiting
//          Waiting
//          Waiting
//          Waiting

//    For this reason companies in production usually use lock.tryLock()

//                boolean acquired = lock.tryLock(
//                        5,
//                        30,
//                        TimeUnit.SECONDS
//                );
//                if(acquired) {
//
//                    try {
//
//                        processOrder();
//
//                    } finally {
//
//                        lock.unlock();
//                    }
//
//                } else {
//
//                    throw new RuntimeException(
//                            "Resource Busy"
//                    );
//                }

//                    tryLock(
//                            waitTime,
//                            leaseTime,
//                            unit
//                    )

//                    waitTime = 5
//                        Wait up to 5 seconds
//                        to acquire lock
//
//                    leaseTime = 30
//                        If acquired,
//                        auto release after 30 sec
//
//                        Request Arrives
//                              │
//                              ▼
//                        Wait For Lock
//                              │
//                              ▼
//                            0 sec
//                            1 sec
//                            2 sec
//                            3 sec
//                            4 sec
//
//                        Lock Available?
//
//                        YES → Acquire
//
//                        NO → Give Up

//NOTE: If we are using lock.tryLock(), then redissson watch dog will be disabled because we have explicitly mentioned a ttl
