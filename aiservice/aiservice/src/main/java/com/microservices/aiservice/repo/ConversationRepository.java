package com.microservices.aiservice.repo;

import com.microservices.aiservice.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository
        extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findByConversationIdOrderByCreatedAtAsc(
            String conversationId
    );
}