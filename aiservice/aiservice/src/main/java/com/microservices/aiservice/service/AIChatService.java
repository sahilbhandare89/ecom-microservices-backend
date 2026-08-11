package com.microservices.aiservice.service;

import com.microservices.aiservice.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final AIShoppingService shoppingService;

    public String chat(ChatRequest request) {

        return shoppingService.shop(request);

    }
}