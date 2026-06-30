package com.example.__spring_redis_spring_cache.repository;

import com.example.__spring_redis_spring_cache.dto.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDTO, Integer> {
}
