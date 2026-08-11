package com.microservices.cart_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/test")
public class RedisTestController {
//
//    private final RedisTemplate<String, Integer> redisTemplate;
//
//    @GetMapping
//    public String test() {
//        redisTemplate.opsForValue().set("message", "Redis Connected");
//        return (String) redisTemplate.opsForValue().get("message");
//    }
}