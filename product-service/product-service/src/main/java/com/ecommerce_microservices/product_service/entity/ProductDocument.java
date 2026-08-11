package com.ecommerce_microservices.product_service.entity;

import com.ecommerce_microservices.product_service.enu.Category;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id;

    private String productName;

    private String productDescription;

    private BigDecimal productPrice;

    private Category category;

    private String productBrand;

    private Integer stockQuantity;

    private Boolean active;

    private List<String> tags;

    private List<ProductImage> images;
}