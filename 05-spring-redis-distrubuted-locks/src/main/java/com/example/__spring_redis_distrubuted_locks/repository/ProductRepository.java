package com.example.__spring_redis_distrubuted_locks.repository;

import com.example.__spring_redis_distrubuted_locks.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
}
