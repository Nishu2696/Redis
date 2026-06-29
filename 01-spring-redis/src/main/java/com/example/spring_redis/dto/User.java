package com.example.spring_redis.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User {
    private String id;
    private String name;
}
