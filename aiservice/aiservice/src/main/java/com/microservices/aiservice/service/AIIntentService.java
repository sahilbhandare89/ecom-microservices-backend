package com.microservices.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microservices.aiservice.dto.AIIntentResponse;
import com.microservices.aiservice.enu.IntentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIIntentService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    private static final Pattern MARKDOWN_JSON_PATTERN = Pattern.compile("^```(?:json)?\\s*([\\s\\S]*?)\\s*```$");

    private static final Pattern PRICE_INDICATOR_PATTERN = Pattern.compile(
            "\\b(under|below|above|over|between|less than|more than|budget|price|cost|\\$|€|£|₹)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final String SYSTEM_PROMPT = """
            You are an AI Intent Extraction Engine for an E-Commerce system.
            Your ONLY task is to parse the user's message and determine their shopping intent into a raw JSON object.

            CRITICAL CONSTRAINTS:
            1. Output ONLY a valid raw JSON object matching the requested format.
            2. DO NOT output markdown code fences, plain text explanations, or conversation context.
            3. 'productNames' MUST BE AN ARRAY OF PLAIN STRINGS ONLY. Do NOT put objects inside productNames.

            INTENT DEFINITIONS & EXAMPLES:
            - ADD_TO_CART: User wants to add an item to cart (e.g., "add iPhone 16 to cart", "buy this item").
            - REMOVE_FROM_CART: User wants to remove/delete an item from cart (e.g., "remove iPhone 20 from my cart", "delete laptop").
            - VIEW_CART: User wants to check or view cart (e.g., "show my cart", "what is in my cart").
            - SEARCH: User is looking for products or asking questions (e.g., "find laptops under 50000").
            - COMPARE: User is asking to compare products.
            - ORDER_STATUS: User is checking order/shipping status.
            - GENERAL_CHAT: Greetings, off-topic chat, or non-actionable general messages.

            Output format (JSON ONLY):
            {
              "intent": "REMOVE_FROM_CART",
              "brand": null,
              "category": null,
              "productName": "iPhone 20",
              "quantity": 1,
              "minPrice": null,
              "maxPrice": null,
              "keyword": "iPhone 20",
              "productNames": ["iPhone 20"]
            }
            """;

    public AIIntentResponse extractIntent(
            String conversationHistory,
            String userQuery
    ) {
        String response = null;

        try {
            // FIX: Force format("json") and low temperature (0.0) so Ollama strictly outputs JSON
            response = chatClient.prompt()
                    .options(OllamaOptions.builder()
                            .format("json")
                            .temperature(0.0)
                            .build())
                    .system(SYSTEM_PROMPT)
                    .user("""
                            Context History:
                            %s

                            Current User Command:
                            %s
                            """.formatted(
                            conversationHistory != null ? conversationHistory : "",
                            userQuery != null ? userQuery : ""
                    ))
                    .call()
                    .content();

            log.info("AI Raw Response:\n{}", response);

            // 1. Clean response if LLM outputs markdown fences
            String cleanedJson = cleanJsonResponse(response);

            // 2. Parse into JsonNode
            JsonNode node = objectMapper.readTree(cleanedJson);

            // 3. Normalize aliases, enums, AND dirty productNames array
            normalize(node);

            // 4. Map to DTO safely
            AIIntentResponse intent = objectMapper.treeToValue(node, AIIntentResponse.class);

            // 5. Sanitize & validate
            intent = sanitize(intent, userQuery);

            return validate(intent);

        } catch (Exception e) {
            log.error("Invalid AI Response received from LLM:\n{}", response, e);

            // Fallback parsing handles user action directly if LLM JSON fails
            return createFallbackIntent(userQuery);
        }
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            return "{}";
        }
        String trimmed = raw.trim();
        var matcher = MARKDOWN_JSON_PATTERN.matcher(trimmed);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return trimmed;
    }

    private void normalize(JsonNode node) {
        if (!(node instanceof ObjectNode objectNode)) {
            return;
        }

        if (objectNode.hasNonNull("intent")) {
            objectNode.put(
                    "intent",
                    normalizeIntent(objectNode.get("intent").asText())
            );
        }

        if (objectNode.hasNonNull("category")) {
            objectNode.put(
                    "category",
                    normalizeCategory(objectNode.get("category").asText())
            );
        }

        normalizeProductNames(objectNode);
    }

    private void normalizeProductNames(ObjectNode objectNode) {
        if (!objectNode.hasNonNull("productNames") || !objectNode.get("productNames").isArray()) {
            return;
        }

        ArrayNode rawProductNames = (ArrayNode) objectNode.get("productNames");
        ArrayNode cleanedProductNames = objectMapper.createArrayNode();

        for (JsonNode item : rawProductNames) {
            if (item.isObject()) {
                if (item.hasNonNull("productName")) {
                    cleanedProductNames.add(item.get("productName").asText());
                } else if (item.hasNonNull("name")) {
                    cleanedProductNames.add(item.get("name").asText());
                } else {
                    cleanedProductNames.add(item.toString());
                }
            } else if (item.isTextual()) {
                cleanedProductNames.add(item.asText());
            }
        }

        objectNode.set("productNames", cleanedProductNames);
    }

    private String normalizeIntent(String value) {
        value = value.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);

        Map<String, String> aliases = Map.ofEntries(
                Map.entry("SEARCH_PRODUCTS", "SEARCH"),
                Map.entry("PRODUCT_SEARCH", "SEARCH"),
                Map.entry("COMPARE_PRODUCTS", "COMPARE"),
                Map.entry("ADD_CART", "ADD_TO_CART"),
                Map.entry("REMOVE_CART", "REMOVE_FROM_CART"),
                Map.entry("SHOW_CART", "VIEW_CART"),
                Map.entry("TRACK_ORDER", "ORDER_STATUS")
        );

        return aliases.getOrDefault(value, value);
    }

    private String normalizeCategory(String value) {
        value = value.trim()
                .replace("-", "_")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);

        Map<String, String> aliases = Map.ofEntries(
                Map.entry("PHONE", "MOBILE"),
                Map.entry("SMARTPHONE", "MOBILE"),
                Map.entry("SMART_PHONE", "MOBILE"),
                Map.entry("MOBILE_PHONE", "MOBILE"),
                Map.entry("IPHONES", "MOBILE"),
                Map.entry("TV", "TELEVISION"),
                Map.entry("HEADSET", "HEADPHONES"),
                Map.entry("EARBUDS", "HEADPHONES"),
                Map.entry("WATCH", "SMARTWATCH"),
                Map.entry("KITCHEN", "HOME_AND_KITCHEN"),
                Map.entry("ELECTRONIC", "ELECTRONICS")
        );

        return aliases.getOrDefault(value, value);
    }

    private AIIntentResponse validate(AIIntentResponse intent) {
        if (intent.maxPrice() != null &&
                intent.maxPrice().compareTo(new BigDecimal("10000000")) > 0) {
            throw new IllegalArgumentException("Invalid maxPrice.");
        }

        if (intent.minPrice() != null &&
                intent.maxPrice() != null &&
                intent.minPrice().compareTo(intent.maxPrice()) > 0) {
            throw new IllegalArgumentException("minPrice cannot exceed maxPrice.");
        }

        return intent;
    }

    private AIIntentResponse sanitize(AIIntentResponse intent, String query) {
        if (query == null) {
            return intent;
        }

        boolean hasPrice = PRICE_INDICATOR_PATTERN.matcher(query).find();

        if (!hasPrice) {
            return new AIIntentResponse(
                    intent.intent(),
                    intent.brand(),
                    intent.category(),
                    intent.quantity(),
                    intent.productName(),
                    null,
                    null,
                    intent.keyword(),
                    intent.productNames() == null ? List.of() : intent.productNames()
            );
        }

        return intent;
    }

    // FIX: Updated Smart Fallback when Ollama fails to respond with JSON
    private AIIntentResponse createFallbackIntent(String query) {
        if (query == null) query = "";
        String lower = query.toLowerCase();

        IntentType intent = IntentType.GENERAL_CHAT;
        if (lower.contains("remove") || lower.contains("delete") || lower.contains("clear")) {
            intent = IntentType.REMOVE_FROM_CART;
        } else if (lower.contains("add") || lower.contains("buy")) {
            intent = IntentType.ADD_TO_CART;
        } else if (lower.contains("cart") || lower.contains("show cart") || lower.contains("view cart")) {
            intent = IntentType.VIEW_CART;
        } else if (lower.contains("search") || lower.contains("find") || lower.contains("show")) {
            intent = IntentType.SEARCH;
        }

        return new AIIntentResponse(
                intent,
                null,
                null,
                1,
                query,
                null,
                null,
                query,
                List.of()
        );
    }
}