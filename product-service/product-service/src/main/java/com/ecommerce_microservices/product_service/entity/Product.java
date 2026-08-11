package com.ecommerce_microservices.product_service.entity;

import com.ecommerce_microservices.product_service.enu.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150)
    private String productName;

    @NotBlank(message = "Product description is required")
    @Size(max = 5000)
    private String productDescription;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal productPrice;


    @NotBlank(message = "Brand is required")
    private String productBrand;

    @NotNull(message = "Category is required")
    private Category category;

    @Builder.Default
    @Valid
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Integer stockQuantity = 0;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;
}