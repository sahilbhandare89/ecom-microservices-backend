package com.microservices.aiservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.aiservice.dto.ChatRequest;
import com.microservices.aiservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIComparisonService {

    private final SemanticSearchService semanticSearchService;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ConversationService conversationService;

    public String compare(ChatRequest request) {

        String conversationId = request.conversationId();
        String query = request.message();

        // Save user message
        conversationService.saveUserMessage(
                conversationId,
                query
        );

        // Get previous conversation
        String history =
                conversationService.getConversationHistory(
                        conversationId
                );

        // Find products using semantic search
        List<ProductResponse> products =
                semanticSearchService.search(query);

        if (products.size() < 2) {

            String answer = "I couldn't find enough products to compare.";

            conversationService.saveAssistantMessage(
                    conversationId,
                    answer
            );

            return answer;
        }

        String catalog = convert(products);

        String answer = chatClient.prompt()

                .system("""
                        You are an AI Product Comparison Assistant.

                        Compare ONLY the supplied products.

                        Never invent specifications.

                        Produce a markdown table.

                        Compare the following attributes:

                        • Product Name
                        • Brand
                        • Category
                        • Price
                        • Stock
                        • Description

                        Finally recommend:

                        - Best Overall
                        - Best Value for Money
                        - Best Premium Choice
                        """)

                .user("""
                        Previous Conversation:

                        %s

                        Customer Request:

                        %s

                        Products:

                        %s
                        """
                        .formatted(
                                history,
                                query,
                                catalog
                        ))

                .call()

                .content();

        // Save assistant response
        conversationService.saveAssistantMessage(
                conversationId,
                answer
        );

        return answer;
    }

    private String convert(List<ProductResponse> products) {

        try {

            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(products);

        } catch (Exception e) {

            throw new RuntimeException("Unable to convert products to JSON", e);
        }
    }
}