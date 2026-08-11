package com.ecommerce_microservices.product_service.mapper;

import com.ecommerce_microservices.product_service.entity.Product;
import com.ecommerce_microservices.product_service.entity.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDocument toDocument(Product product) {

        return ProductDocument.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .productPrice(product.getProductPrice())
                .category(product.getCategory())
                .productBrand(product.getProductBrand())
                .active(product.getActive())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}