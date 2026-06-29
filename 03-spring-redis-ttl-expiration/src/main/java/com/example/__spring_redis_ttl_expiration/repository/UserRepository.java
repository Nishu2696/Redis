package com.example.__spring_redis_ttl_expiration.repository;

import com.example.__spring_redis_ttl_expiration.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
}
