package com.microservices.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ComparisonRequest(

        @NotBlank
        String conversationId,

        @NotBlank
        String message

) {
}