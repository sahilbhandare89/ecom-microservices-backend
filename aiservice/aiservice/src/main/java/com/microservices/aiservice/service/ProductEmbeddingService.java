package com.microservices.aiservice.service;

import com.microservices.aiservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

    private final ProductService productService;
    private final VectorStore vectorStore;

    public void indexAllProducts() {

        List<ProductResponse> products = productService.getAllProducts();

        for (ProductResponse product : products) {

            String content = """
                    Product Name: %s
                    Brand: %s
                    Category: %s
                    Description: %s
                    Price: %s
                    Stock: %s
                    """
                    .formatted(
                            safe(product.getProductName()),
                            safe(product.getProductBrand()),
                            safe(product.getProductCategory()),
                            safe(product.getProductDescription()),
                            safe(product.getProductPrice()),
                            safe(product.getStockQuantity())
                    );

            Map<String, Object> metadata = new HashMap<>();

            put(metadata, "id", product.getId());
            put(metadata, "name", product.getProductName());
            put(metadata, "brand", product.getProductBrand());
            put(metadata, "category", product.getProductCategory());
            put(metadata, "description", product.getProductDescription());
            put(metadata, "price", product.getProductPrice());
            put(metadata, "stock", product.getStockQuantity());

            System.out.println("============== PRODUCT ==============");
            System.out.println("ID          : " + product.getId());
            System.out.println("NAME        : " + product.getProductName());
            System.out.println("DESCRIPTION : " + product.getProductDescription());
            System.out.println("BRAND       : " + product.getProductBrand());
            System.out.println("CATEGORY    : " + product.getProductCategory());
            System.out.println("PRICE       : " + product.getProductPrice());
            System.out.println("STOCK       : " + product.getStockQuantity());

            vectorStore.add(List.of(
                    new Document(content, metadata)
            ));
            System.out.println(metadata);
            System.out.println(content);
        }
    }

    private void put(Map<String, Object> metadata,
                     String key,
                     Object value) {

        if (value != null) {
            metadata.put(key, value);
        }
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }
}