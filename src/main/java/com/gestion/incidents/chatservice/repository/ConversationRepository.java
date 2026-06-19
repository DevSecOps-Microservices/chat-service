package com.gestion.incidents.chatservice.repository;

import com.gestion.incidents.chatservice.model.ChatConversation;
import com.gestion.incidents.chatservice.model.StatutConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<ChatConversation, UUID> {
    List<ChatConversation> findByUtilisateurIdOrderByDateCreationDesc(UUID utilisateurId);
    List<ChatConversation> findByStatutOrderByDateCreationDesc(StatutConversation statut);
}
