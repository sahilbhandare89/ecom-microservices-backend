package com.ecommerce_microservices.product_service.dto;

import com.ecommerce_microservices.product_service.entity.ProductImage;
import com.ecommerce_microservices.product_service.enu.Category;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private Integer stockQuantity;
    private Category category;
    private String productBrand;
    private List<String> tags;
    private List<ProductImage> images;
}