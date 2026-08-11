package com.microservices.aiservice.controller;

import com.microservices.aiservice.dto.ProductDto;
import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/vector")
@RequiredArgsConstructor
public class VectorTestController {

    private final VectorStore vectorStore;

    @PostMapping
    public String test() {

        vectorStore.add(List.of(
                new Document("Samsung Galaxy S24 Ultra"),
                new Document("Apple iPhone 16 Pro Max"),
                new Document("Google Pixel 9 Pro")
        ));

        return "Stored!";
    }
}