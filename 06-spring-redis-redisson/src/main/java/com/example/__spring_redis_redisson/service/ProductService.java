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
}
