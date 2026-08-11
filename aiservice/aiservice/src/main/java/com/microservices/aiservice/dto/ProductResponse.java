package com.microservices.aiservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

        private String productBrand;

        private String productCategory;

        private List<ImageDto> images;

        /**
         * Extracts the primary image URL, or falls back to the first available image URL if none is marked primary.
         */
        public String getPrimaryImageUrl() {
                if (images == null || images.isEmpty()) {
                        return null;
                }
                return images.stream()
                        .filter(ImageDto::isPrimaryImage)
                        .map(ImageDto::getUrl)
                        .findFirst()
                        .orElseGet(() -> images.get(0).getUrl());
        }

        @Getter
        @Setter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ImageDto {

                private String objectKey;

                private String url;

                private String fileName;

                private String contentType;

                @JsonProperty("primaryImage")
                private boolean primaryImage;

                private Long size;

                private boolean primary;
        }
}