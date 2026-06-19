package com.gestion.incidents.chatservice.repository;

import com.gestion.incidents.chatservice.model.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, UUID> {
    List<Suggestion> findByConversationId(UUID conversationId);
}
