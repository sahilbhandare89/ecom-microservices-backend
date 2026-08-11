package com.microservices.aiservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conversationId;

    /**
     * USER or ASSISTANT
     */
    private String role;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;
}