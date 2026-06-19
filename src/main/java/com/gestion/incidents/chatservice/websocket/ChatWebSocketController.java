package com.gestion.incidents.chatservice.websocket;

import com.gestion.incidents.chatservice.dto.*;
import com.gestion.incidents.chatservice.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/**
 * Contrôleur WebSocket pour le chat temps réel.
 *
 * Flux WebSocket :
 *  Client → /app/chat/demarrer  → Bot répond sur /topic/chat/{conversationId}
 *  Client → /app/chat/message   → Bot répond sur /topic/chat/{conversationId}
 */
@Controller
public class ChatWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

    private final ConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(ConversationService conversationService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Démarre une nouvelle conversation via WebSocket
     * Client envoie vers : /app/chat/demarrer
     * Réponse publiée sur : /topic/chat/new
     */
    @MessageMapping("/chat/demarrer")
    public void demarrerConversation(@Payload NouvelleConversationDTO dto) {
        log.info("WebSocket - Nouvelle conversation: {}", dto.getUtilisateurNom());
        try {
            MessageResponseDTO reponse = conversationService.demarrerConversation(dto);
            messagingTemplate.convertAndSend(
                "/topic/chat/" + reponse.getConversationId(), reponse);
        } catch (Exception e) {
            log.error("Erreur WebSocket demarrer: {}", e.getMessage());
        }
    }

    /**
     * Envoie un message dans une conversation via WebSocket
     * Client envoie vers : /app/chat/message
     * Réponse publiée sur : /topic/chat/{conversationId}
     */
    @MessageMapping("/chat/message")
    public void envoyerMessage(@Payload MessageDTO dto) {
        log.info("WebSocket - Message pour conversation: {}", dto.getConversationId());
        try {
            MessageResponseDTO reponse = conversationService.envoyerMessage(dto);
            messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getConversationId(), reponse);
        } catch (Exception e) {
            log.error("Erreur WebSocket message: {}", e.getMessage());
            WebSocketError erreur = new WebSocketError("Erreur: " + e.getMessage());
            messagingTemplate.convertAndSend(
                "/topic/chat/" + dto.getConversationId() + "/erreur", erreur);
        }
    }

    // Classe interne pour les erreurs WebSocket
    public static class WebSocketError {
        private String message;
        public WebSocketError(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
