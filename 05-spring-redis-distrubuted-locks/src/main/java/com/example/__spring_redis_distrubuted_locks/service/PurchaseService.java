package com.example.__spring_redis_distrubuted_locks.service;

import com.example.__spring_redis_distrubuted_locks.entity.ProductEntity;
import com.example.__spring_redis_distrubuted_locks.redis.DistrubutedLockService;
import com.example.__spring_redis_distrubuted_locks.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final StringRedisTemplate stringRedisTemplate;
    private ProductRepository productRepository;

    private final DistrubutedLockService distrubutedLockService;

    // This is used, if the whole happens properly then only store the changes in the db,
    // if the app crashes in between of performing the operation, then everything will be reverted back to the old state
    @Transactional
    public void purchase(Integer productId) {
        String lockKey = "lock:product:" + productId;
        String ownerId = UUID.randomUUID().toString();
        Boolean acquired =  distrubutedLockService
                .acquireLock(
                        lockKey,
                        ownerId
                );

        if (!acquired) {
            throw new RuntimeException("Purchase already in progress");
        }

        try {
            ProductEntity product = productRepository.findById(productId).orElseThrow();
            if (product.getStock() <= 0) {
                throw new RuntimeException("Out of stock");
            }
            product.setStock(
                    product.getStock() - 1
            );
            productRepository.save(product);
        }
        finally {
            distrubutedLockService.releaseLock(
                    lockKey,
                    ownerId
            );
        }
    }
}
