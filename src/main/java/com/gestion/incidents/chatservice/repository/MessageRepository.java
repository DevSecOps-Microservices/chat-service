package com.gestion.incidents.chatservice.repository;

import com.gestion.incidents.chatservice.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(UUID conversationId);
}
