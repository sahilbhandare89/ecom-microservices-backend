package com.microservices.aiservice.service;

import com.microservices.aiservice.client.CartClient;
import com.microservices.aiservice.dto.AIIntentResponse;
import com.microservices.aiservice.dto.ChatRequest;
import com.microservices.aiservice.dto.ProductResponse;
import com.microservices.aiservice.dto.cart.AddToCartRequest;
import com.microservices.aiservice.dto.cart.CartItemResponse;
import com.microservices.aiservice.dto.cart.CartResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartAgentService {

    private final CartClient cartClient;
    private final SemanticSearchService semanticSearchService;
    private final ConversationService conversationService;

    // ==========================================
    // 1. ADD TO CART
    // ==========================================
    public String addToCart(ChatRequest request, AIIntentResponse intent) {

        if (isInvalidUser(request.userId())) {
            return saveAndReturn(
                    request.conversationId(),
                    "Please sign in or provide a valid user ID to manage your shopping cart."
            );
        }

        String searchQuery = extractSearchQuery(intent, request.message());
        List<ProductResponse> products = semanticSearchService.search(searchQuery);

        if (products.isEmpty()) {
            return saveAndReturn(
                    request.conversationId(),
                    "I couldn't find any product matching '" + searchQuery + "' in our catalog."
            );
        }

        ProductResponse product = products.get(0);
        int quantity = (intent.quantity() != null && intent.quantity() > 0) ? intent.quantity() : 1;

        try {
            AddToCartRequest cartRequest = new AddToCartRequest(
                    product.getId(),
                    quantity
            );

            cartClient.addToCart(request.userId(), cartRequest);

            String responseMessage = String.format(
                    "Added %d x '%s' to your cart successfully!",
                    quantity,
                    product.getProductName()
            );

            return saveAndReturn(request.conversationId(), responseMessage);

        } catch (Exception e) {
            log.error("Failed to add product ID [{}] to cart for user [{}] via Feign Client",
                    product.getId(), request.userId(), e);

            return saveAndReturn(
                    request.conversationId(),
                    "I found " + product.getProductName() + ", but encountered an issue updating your cart. Please try again in a moment."
            );
        }
    }

    // ==========================================
    // 2. VIEW CART
    // ==========================================
    public String viewCart(ChatRequest request) {

        if (isInvalidUser(request.userId())) {
            return saveAndReturn(
                    request.conversationId(),
                    "Please sign in to view your shopping cart."
            );
        }

        try {
            CartResponse cart = cartClient.getCart(request.userId());

            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return saveAndReturn(
                        request.conversationId(),
                        "Your shopping cart is currently empty."
                );
            }

            StringBuilder sb = new StringBuilder("Here is what's currently in your cart:\n\n");

            int index = 1;
            for (CartItemResponse item : cart.getItems()) {
                sb.append(String.format("%d. %s (Qty: %d) - ₹%.2f\n",
                        index++,
                        item.getProductName() != null ? item.getProductName() : "Product ID: " + item.getProductId(),
                        item.getQuantity(),
                        item.getPrice() != null ? item.getPrice() : 0.0
                ));
            }

            if (cart.getTotalPrice() != null) {
                sb.append(String.format("\n**Total Price:** ₹%.2f", cart.getTotalPrice()));
            }

            return saveAndReturn(request.conversationId(), sb.toString());

        } catch (Exception e) {
            log.error("Failed to fetch cart for user [{}] via Feign Client", request.userId(), e);

            return saveAndReturn(
                    request.conversationId(),
                    "I encountered an issue fetching your cart items. Please try again in a moment."
            );
        }
    }

    // ==========================================
    // 3. REMOVE FROM CART
    // ==========================================
    public String removeFromCart(ChatRequest request, AIIntentResponse intent) {

        if (isInvalidUser(request.userId())) {
            return saveAndReturn(
                    request.conversationId(),
                    "Please sign in to modify your shopping cart."
            );
        }

        try {
            CartResponse cart = cartClient.getCart(request.userId());

            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return saveAndReturn(
                        request.conversationId(),
                        "Your cart is already empty, so there is nothing to remove."
                );
            }

            String targetQuery = extractSearchQuery(intent, request.message()).toLowerCase();
            CartItemResponse itemToRemove = findMatchingCartItem(cart.getItems(), targetQuery);

            String productIdToRemove;
            String productNameToRemove;

            if (itemToRemove != null) {
                productIdToRemove = itemToRemove.getProductId();
                productNameToRemove = itemToRemove.getProductName() != null ? itemToRemove.getProductName() : "item";
            } else {
                List<ProductResponse> products = semanticSearchService.search(targetQuery);
                if (products.isEmpty()) {
                    return saveAndReturn(
                            request.conversationId(),
                            "I couldn't identify which item you want to remove. Here is what's currently in your cart:\n" + formatCartItemList(cart.getItems())
                    );
                }
                ProductResponse matchedProduct = products.get(0);
                productIdToRemove = matchedProduct.getId();
                productNameToRemove = matchedProduct.getProductName();
            }

            cartClient.removeItem(request.userId(), productIdToRemove);

            String responseMessage = String.format("'%s' has been removed from your cart.", productNameToRemove);
            return saveAndReturn(request.conversationId(), responseMessage);

        } catch (Exception e) {
            log.error("Failed to remove item from cart for user [{}] via Feign Client", request.userId(), e);

            return saveAndReturn(
                    request.conversationId(),
                    "An error occurred while trying to remove the item from your cart. Please try again."
            );
        }
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================
    private boolean isInvalidUser(String userId) {
        return userId == null || userId.isBlank();
    }

    private CartItemResponse findMatchingCartItem(List<CartItemResponse> items, String query) {
        for (CartItemResponse item : items) {
            if (item.getProductName() != null && item.getProductName().toLowerCase().contains(query)) {
                return item;
            }
        }
        return null;
    }

    private String formatCartItemList(List<CartItemResponse> items) {
        return items.stream()
                .map(i -> "- " + (i.getProductName() != null ? i.getProductName() : i.getProductId()))
                .collect(Collectors.joining("\n"));
    }

    private String extractSearchQuery(AIIntentResponse intent, String rawMessage) {
        if (intent.productName() != null && !intent.productName().isBlank()) {
            return intent.productName();
        }
        if (intent.keyword() != null && !intent.keyword().isBlank()) {
            return intent.keyword();
        }
        if (intent.productNames() != null && !intent.productNames().isEmpty()) {
            return intent.productNames().get(0);
        }
        return rawMessage;
    }

    private String saveAndReturn(String conversationId, String answer) {
        conversationService.saveAssistantMessage(conversationId, answer);
        return answer;
    }
}