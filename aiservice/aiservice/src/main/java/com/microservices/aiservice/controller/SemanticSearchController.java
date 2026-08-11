package com.microservices.aiservice.controller;

import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.service.SemanticSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SemanticSearchController {

    private final SemanticSearchService semanticSearchService;

    @PostMapping
    public List<ProductResponse> search(
            @RequestBody String query
    ) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Remove surrounding quotes if passed as JSON string ("iPhone 16" -> iPhone 16)
        String cleanedQuery = query.trim();
        if (cleanedQuery.startsWith("\"") && cleanedQuery.endsWith("\"") && cleanedQuery.length() > 1) {
            cleanedQuery = cleanedQuery.substring(1, cleanedQuery.length() - 1).trim();
        }

        return semanticSearchService.search(cleanedQuery);
    }
}