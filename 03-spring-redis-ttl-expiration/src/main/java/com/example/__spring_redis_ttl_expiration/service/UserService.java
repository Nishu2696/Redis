package com.example.__spring_redis_ttl_expiration.service;

import com.example.__spring_redis_ttl_expiration.dto.User;
import com.example.__spring_redis_ttl_expiration.entity.UserEntity;
import com.example.__spring_redis_ttl_expiration.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;

    public User getUser(String id) {
        User cachedUser = userCacheService.getUser(id);

//        if found in redis for this particular id, then return the same
        if (cachedUser != null) {
            return cachedUser;
        }

//        cache miss, hit DB
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());

        userCacheService.cachedUser(newUser);
        return newUser;
    }
}
