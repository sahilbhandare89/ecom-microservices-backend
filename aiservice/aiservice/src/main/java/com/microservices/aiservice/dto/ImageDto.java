package com.microservices.aiservice.dto;

public record ImageDto(

        String objectKey,
        String url,
        String fileName,
        String contentType,
        Boolean primaryImage,
        Long size,
        Boolean primary

) {
}