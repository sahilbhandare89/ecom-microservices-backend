package com.microservices.aiservice.service;

import com.microservices.aiservice.client.ProductClient;
import com.microservices.aiservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchService {

    private final VectorStore vectorStore;
    private final ProductClient productClient;

    // Adjusted threshold for local embedding models (Ollama/Nomic/MiniLM)
    private static final double SIMILARITY_THRESHOLD = 0.30;

    public List<ProductResponse> search(String query) {

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .build()
        );

        log.info("Found {} vector matches for query: '{}'", documents.size(), query);

        if (documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
                .map(this::toProduct)
                .filter(Objects::nonNull)
                .toList();
    }

    private ProductResponse toProduct(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        String productId = (String) metadata.get("id");

        if (productId == null) {
            return null;
        }

        try {
            // Re-hydrate live product details (with full image list) from product-service
            return productClient.getProductById(productId);
        } catch (Exception e) {
            log.warn("Failed to fetch product details via Feign for ID {}: {}", productId, e.getMessage());

            // Fallback construct if product-service call fails
            ProductResponse product = new ProductResponse();
            product.setId(productId);
            product.setProductName((String) metadata.get("name"));
            product.setProductDescription((String) metadata.get("description"));

            if (metadata.get("price") != null) {
                product.setProductPrice(new BigDecimal(metadata.get("price").toString()));
            }
            if (metadata.get("stock") != null) {
                product.setStockQuantity(Integer.parseInt(metadata.get("stock").toString()));
            }
            return product;
        }
    }
}