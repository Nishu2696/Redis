package com.example.__spring_redis_redisson.controller;

import com.example.__spring_redis_redisson.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping("/{id}/purchase")
    public String purchase(@PathVariable Long id) {
        productService.purchase(id);
        return  "Purchased";
    }
}
