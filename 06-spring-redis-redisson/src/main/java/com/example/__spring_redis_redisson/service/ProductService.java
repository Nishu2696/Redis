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
//    say it internally sets the TTL as 30s, so for every second this watch dog will be monitoring
//    after 30 s if the lock is not freed up it will extend the ttl again by 30s
//    so in this way dynamic TTL is set

//    

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
