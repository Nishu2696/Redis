package com.example.__spring_redis_lua_scripting.controller;

import com.example.__spring_redis_lua_scripting.service.RedisLuaScriptService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AllArgsConstructor(onConstructor_ = @Autowired)
public class RedisLuaScriptController {
    private final RedisLuaScriptService redisLuaScriptService;
    @GetMapping("/{id}")
    public String getUser(@PathVariable String id) throws Exception {
        boolean allowed = redisLuaScriptService.allowRequest(id);
        if (!allowed) {
            throw new Exception("Rate Limit Exceeded");
        }
        else {
            return "User Details";
        }
    }
}
