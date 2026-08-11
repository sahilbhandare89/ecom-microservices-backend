package com.microservices.aiservice.controller;

import com.microservices.aiservice.service.ProductEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/embedding")
@RequiredArgsConstructor
public class EmbeddingController {

    private final ProductEmbeddingService productEmbeddingService;

    @PostMapping("/products")
    public String indexProducts() {

        productEmbeddingService.indexAllProducts();

        return "Products Indexed Successfully";
    }
}