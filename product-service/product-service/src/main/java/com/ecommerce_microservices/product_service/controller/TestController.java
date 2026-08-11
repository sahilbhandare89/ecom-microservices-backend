package com.ecommerce_microservices.product_service.controller;

import com.ecommerce_microservices.product_service.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final ProductRepo productRepo;

    @GetMapping("/test")
    public long test() {
        return productRepo.count();
    }
}