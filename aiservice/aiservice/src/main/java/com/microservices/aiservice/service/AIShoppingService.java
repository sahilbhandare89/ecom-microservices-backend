package com.microservices.aiservice.service;

import com.microservices.aiservice.dto.AIIntentResponse;
import com.microservices.aiservice.dto.ChatRequest;
import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.enu.IntentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIShoppingService {

    private final AIIntentService aiIntentService;
    private final ProductService productService;
    private final SemanticSearchService semanticSearchService;
    private final AIRecommendationService recommendationService;
    private final ConversationService conversationService;

    // Inject CartAgentService
    private final CartAgentService cartAgentService;

    private static final String DEFAULT_NO_PRODUCTS_FOUND = "No matching products were found in our catalog.";

    public String shop(ChatRequest request) {

        // 1. Load history BEFORE saving current message to avoid duplicate message context in LLM prompt
        String history = conversationService.getConversationHistory(request.conversationId());

        // 2. Save current user message
        conversationService.saveUserMessage(
                request.conversationId(),
                request.message()
        );

        // 3. Extract AI intent
        AIIntentResponse intent = aiIntentService.extractIntent(
                history,
                request.message()
        );

        // 4. Handle non-search intents gracefully (e.g., CART, GENERAL_CHAT)
        if (intent.intent() != null && isCartOrChatIntent(intent.intent())) {
            return handleNonSearchIntents(request, intent, history);
        }

        // 5. Structured database search wrapped with resilient error handling
        List<ProductResponse> products = safeFilterProducts(intent);

        // 6. Semantic vector search fallback if structured search returned nothing
        if (products.isEmpty()) {
            products = safeSemanticSearch(request.message(), intent);
        }

        // 7. Handle empty catalog / no matches scenario
        if (products.isEmpty()) {
            conversationService.saveAssistantMessage(
                    request.conversationId(),
                    DEFAULT_NO_PRODUCTS_FOUND
            );
            return DEFAULT_NO_PRODUCTS_FOUND;
        }

        // 8. Generate contextual recommendation answer using LLM
        String answer = recommendationService.recommend(
                history,
                request.message(),
                products
        );

        // 9. Persist assistant response
        conversationService.saveAssistantMessage(
                request.conversationId(),
                answer
        );

        return answer;
    }

    /**
     * Safely queries ProductService via Feign with fallback handling to prevent 500 downstream errors
     */
    private List<ProductResponse> safeFilterProducts(AIIntentResponse intent) {
        try {
            // Clean keyword parameter: prefer extracted keyword -> productName -> brand -> null
            String keywordToSearch = intent.keyword();
            if (keywordToSearch == null && intent.productName() != null) {
                keywordToSearch = intent.productName();
            }

            return productService.filterProducts(
                    intent.brand(),
                    intent.category(),
                    intent.minPrice(),
                    intent.maxPrice(),
                    keywordToSearch
            );
        } catch (Exception e) {
            log.error("Feign call to product-service failed during filterProducts: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Safely executes vector search fallback
     */
    private List<ProductResponse> safeSemanticSearch(String message, AIIntentResponse intent) {
        try {
            // Use specific extracted product name or keyword if available, otherwise raw message
            String query = intent.keyword() != null ? intent.keyword() : message;
            return semanticSearchService.search(query);
        } catch (Exception e) {
            log.error("Semantic search failed: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private boolean isCartOrChatIntent(IntentType intentType) {
        return intentType == IntentType.ADD_TO_CART
                || intentType == IntentType.REMOVE_FROM_CART
                || intentType == IntentType.VIEW_CART
                || intentType == IntentType.ORDER_STATUS
                || intentType == IntentType.GENERAL_CHAT;
    }

    private String handleNonSearchIntents(ChatRequest request, AIIntentResponse intent, String history) {

        switch (intent.intent()) {

            case ADD_TO_CART:
                return cartAgentService.addToCart(request, intent);

            case VIEW_CART:
                return cartAgentService.viewCart(request);

            case REMOVE_FROM_CART:
                return cartAgentService.removeFromCart(request, intent);

            case GENERAL_CHAT:
                String chatResponse = "How can I help you with your shopping today? You can search for products, compare items, or manage your cart!";
                conversationService.saveAssistantMessage(request.conversationId(), chatResponse);
                return chatResponse;

            default:
                String fallbackResponse = "I've received your " + intent.intent() + " request, but cannot perform that action right now.";
                conversationService.saveAssistantMessage(request.conversationId(), fallbackResponse);
                return fallbackResponse;
        }
    }
}