package com.microservices.aiservice.service;

import com.microservices.aiservice.dto.AIIntentResponse;
import com.microservices.aiservice.dto.ChatRequest;
import com.microservices.aiservice.entity.ConversationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AIAgentService {

    private final AIIntentService intentService;
    private final AIShoppingService shoppingService;
    private final AIComparisonService comparisonService;
    private final ConversationService conversationService;

    public String chat(ChatRequest request) {

        // Save current user message
        conversationService.saveUserMessage(
                request.conversationId(),
                request.message()
        );

        // Build conversation history
        String history =
                conversationService.getConversationHistory(
                        request.conversationId()
                );

        // Extract intent
        AIIntentResponse intent =
                intentService.extractIntent(
                        history,
                        request.message()
                );

        String response = switch (intent.intent()) {

            case SEARCH ->
                    shoppingService.shop(request);

            case COMPARE ->
                    comparisonService.compare(request);

            case ADD_TO_CART ->
                    "Add to cart feature is coming soon.";

            case REMOVE_FROM_CART ->
                    "Remove from cart feature is coming soon.";

            case VIEW_CART ->
                    "View cart feature is coming soon.";

            case ORDER_STATUS ->
                    "Order tracking feature is coming soon.";

            case GENERAL_CHAT ->
                    "General chat feature is coming soon.";
        };

        // Save assistant response
        conversationService.saveAssistantMessage(
                request.conversationId(),
                response
        );

        return response;
    }
}