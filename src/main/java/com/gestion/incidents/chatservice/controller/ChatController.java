package com.gestion.incidents.chatservice.controller;

import com.gestion.incidents.chatservice.dto.*;
import com.gestion.incidents.chatservice.service.ChatBotService;
import com.gestion.incidents.chatservice.service.ConversationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ConversationService conversationService;
    private final ChatBotService botService;

    public ChatController(ConversationService conversationService,
                          ChatBotService botService) {
        this.conversationService = conversationService;
        this.botService = botService;
    }

    /**
     * POST /api/chat/demarrer
     * Démarre une nouvelle conversation avec un message initial
     */
    @PostMapping("/demarrer")
    public ResponseEntity<MessageResponseDTO> demarrerConversation(
            @Valid @RequestBody NouvelleConversationDTO dto) {
        log.info("Nouvelle conversation - utilisateur: {}", dto.getUtilisateurNom());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(conversationService.demarrerConversation(dto));
    }

    /**
     * POST /api/chat/message
     * Envoie un message dans une conversation existante
     */
    @PostMapping("/message")
    public ResponseEntity<MessageResponseDTO> envoyerMessage(
            @Valid @RequestBody MessageDTO dto) {
        log.info("Message reçu - conversationId: {}", dto.getConversationId());
        return ResponseEntity.ok(conversationService.envoyerMessage(dto));
    }

    /**
     * GET /api/chat/conversation/{id}
     * Obtenir une conversation avec son historique complet
     */
    @GetMapping("/conversation/{id}")
    public ResponseEntity<ConversationDTO> obtenirConversation(@PathVariable UUID id) {
        return ResponseEntity.ok(conversationService.obtenirConversation(id));
    }

    /**
     * GET /api/chat/conversations/{utilisateurId}
     * Lister toutes les conversations d'un utilisateur
     */
    @GetMapping("/conversations/{utilisateurId}")
    public ResponseEntity<List<ConversationDTO>> listerConversations(
            @PathVariable UUID utilisateurId) {
        return ResponseEntity.ok(conversationService.listerConversations(utilisateurId));
    }

    /**
     * GET /api/chat/accueil
     * Message de bienvenue du bot (pour initialiser l'interface)
     */
    @GetMapping("/accueil")
    public ResponseEntity<String> messageAccueil() {
        return ResponseEntity.ok(botService.messageAccueil());
    }
}
