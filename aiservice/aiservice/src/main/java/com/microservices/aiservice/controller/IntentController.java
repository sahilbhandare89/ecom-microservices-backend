package com.microservices.aiservice.controller;

import com.microservices.aiservice.dto.AIIntentResponse;
import com.microservices.aiservice.service.AIIntentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class IntentController {

    private final AIIntentService intentService;

    @PostMapping("/intent")
    public AIIntentResponse intent(
            @RequestBody String query
    ) {

        return intentService.extractIntent(
                "",      // No conversation history for testing
                query
        );
    }
}