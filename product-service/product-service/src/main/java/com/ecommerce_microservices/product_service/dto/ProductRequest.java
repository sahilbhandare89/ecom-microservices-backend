package com.ecommerce_microservices.product_service.dto;

import com.ecommerce_microservices.product_service.enu.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
    private String productName;

    @NotBlank(message = "Product description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String productDescription;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal productPrice;

    @NotNull(message = "Category is required")
    private Category category;

    @NotBlank(message = "Brand is required")
    private String productBrand;

    @Builder.Default
    private List<String> tags = List.of();

    @Builder.Default
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;
}