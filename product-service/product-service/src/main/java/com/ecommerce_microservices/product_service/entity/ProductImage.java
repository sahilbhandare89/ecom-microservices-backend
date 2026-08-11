package com.ecommerce_microservices.product_service.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    // Object name in MinIO
    private String objectKey;

    // Public or pre-signed URL
    private String url;

    // Original file name
    private String fileName;

    // image/jpeg, image/png...
    private String contentType;

    @Builder.Default
    private Boolean primaryImage = false;

    // Bytes
    private Long size;

    // First image shown to users
    @Builder.Default
    private Boolean primary = false;
}