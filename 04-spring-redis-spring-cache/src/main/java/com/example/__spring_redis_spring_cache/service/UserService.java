package com.example.__spring_redis_spring_cache.service;

import com.example.__spring_redis_spring_cache.dto.UserDTO;
import com.example.__spring_redis_spring_cache.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

//    NOTE: @Cacheable and @CacheEvict will only work if they are directly called

//    @Cacheable is a spring cache annotation
//    this key will internally work as redisTemplate.opsForValue(key, value)
//    Internally this how spring works
//    Spring Proxy
//     ↓
//    CacheManager
//     ↓
//    Redis
//     ↓
//    Cache Hit?
    @Cacheable(
            value="users",
            key="#id"
    )
    public UserDTO getUser(Integer id) {
        System.out.println("Reading from DB");
        return userRepository.findById(id).orElseThrow();
    }
//    Internally the full flow of above code
//    when first time getUser(101) is hit, this key is not available in the redis
//    so the flow is getUser(101) -> redis miss -> hit db -> get data -> redisTemplate.opsForValue().set(key: 101, value: user), how this is called, this is because we are using @Cacheable annotation
//    after this if we hit again getUser(101) -> spring cache has the value -> return from here itself

//    in the above example key is using single value
//    We can multiple combination based key name as well
    @Cacheable(
            value = "users",
            key = "#userId + ':' + #region"
    )
    public UserDTO getUser(
            Integer userId,
            String region) {
        System.out.println("Reading from DB");
        return userRepository.findById(userId).orElseThrow();
    }
//    @CacheEvict
//    Used when data changes.
//    Delete User
//        ↓
//    Delete Cache
    @CacheEvict(
            value = "users",
            key = "#id"
    )
    public void deleteUser(Integer id) {

        userRepository.deleteById(id);
    }
//    We will be using @CacheEvict for updating purpose as well
//      Update DB
//          ↓
//      Remove Cache
//          ↓
//      Next Read
//          ↓
//          DB
//          ↓
//      Fresh Cache
    @CacheEvict(
            value = "users",
            key = "#id"
    )
    public UserDTO updateUser(
            Integer id,
            String name) {

        UserDTO user =
                userRepository.findById(id)
                        .orElseThrow();

        user.setUsername(name);

        return userRepository.save(user);
    }
}
