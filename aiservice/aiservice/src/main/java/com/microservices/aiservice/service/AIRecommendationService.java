package com.microservices.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.aiservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public String recommend(
            String conversationHistory,
            String query,
            List<ProductResponse> products
    ) {

        String catalog = convert(products);

        String systemPrompt = """
                You are an AI Shopping Assistant for an e-commerce website.

                Rules:
                - Use the previous conversation to understand the customer's intent.
                - Recommend ONLY products from the supplied catalog.
                - Never invent products.
                - If no suitable product exists, clearly say so.
                - Rank products from best to worst.
                - Explain briefly why each product matches.

                For every recommendation include:
                - Product Name
                - Brand
                - Price
                - Stock
                """;

        String userPrompt = """
                Previous Conversation:

                %s

                Current Customer Query:

                %s

                Available Products:

                %s
                """
                .formatted(
                        conversationHistory,
                        query,
                        catalog
                );

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    private String convert(List<ProductResponse> products) {

        try {

            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(products);

        } catch (Exception e) {

            throw new RuntimeException("Failed to serialize products", e);
        }
    }
}