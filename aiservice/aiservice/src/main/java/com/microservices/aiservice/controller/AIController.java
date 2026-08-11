package com.microservices.aiservice.controller;

import com.microservices.aiservice.dto.ChatRequest;
import com.microservices.aiservice.dto.ChatResponse;
import com.microservices.aiservice.service.AIComparisonService;
import com.microservices.aiservice.service.AIShoppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIShoppingService shoppingService;
    private final AIComparisonService comparisonService;

    @PostMapping("/chat")
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request
    ) {

        String response = shoppingService.shop(request);

        return new ChatResponse(response);
    }

    @PostMapping("/shop")
    public ResponseEntity<String> shop(
            @Valid @RequestBody ChatRequest request
    ) {

        return ResponseEntity.ok(
                shoppingService.shop(request)
        );
    }

    @PostMapping("/compare")
    public ResponseEntity<String> compare(
            @Valid @RequestBody ChatRequest request
    ) {

        return ResponseEntity.ok(
                comparisonService.compare(request)
        );
    }

}