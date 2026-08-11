package com.microservices.aiservice.service;

import com.microservices.aiservice.entity.ConversationMessage;
import com.microservices.aiservice.repo.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository repository;

    public void saveUserMessage(String conversationId, String message) {

        repository.save(
                ConversationMessage.builder()
                        .conversationId(conversationId)
                        .role("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    public void saveAssistantMessage(String conversationId, String message) {

        repository.save(
                ConversationMessage.builder()
                        .conversationId(conversationId)
                        .role("ASSISTANT")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    public List<ConversationMessage> getConversation(String conversationId) {

        return repository.findByConversationIdOrderByCreatedAtAsc(
                conversationId
        );
    }

    public String getConversationHistory(String conversationId) {

        List<ConversationMessage> history =
                getConversation(conversationId);

        if (history.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (ConversationMessage message : history) {

            builder.append(message.getRole())
                    .append(": ")
                    .append(message.getMessage())
                    .append(System.lineSeparator());
        }

        return builder.toString();
    }

    public void clearConversation(String conversationId) {

        List<ConversationMessage> messages =
                repository.findByConversationIdOrderByCreatedAtAsc(
                        conversationId
                );

        repository.deleteAll(messages);
    }
}