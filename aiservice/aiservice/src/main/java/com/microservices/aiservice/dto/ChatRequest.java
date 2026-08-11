package com.microservices.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(

        @NotBlank(message = "Conversation Id cannot be empty")
        String conversationId,

        @NotBlank(message = "User Id cannot be empty")
        String userId,

        @NotBlank(message = "Message cannot be empty")
        String message

) {
}