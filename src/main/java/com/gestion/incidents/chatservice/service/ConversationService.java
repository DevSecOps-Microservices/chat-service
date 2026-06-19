package com.gestion.incidents.chatservice.service;

import com.gestion.incidents.chatservice.dto.*;
import com.gestion.incidents.chatservice.exception.ConversationNotFoundException;
import com.gestion.incidents.chatservice.model.*;
import com.gestion.incidents.chatservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConversationService {

    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final SuggestionRepository suggestionRepo;
    private final ChatBotService botService;

    public ConversationService(ConversationRepository conversationRepo,
                               MessageRepository messageRepo,
                               SuggestionRepository suggestionRepo,
                               ChatBotService botService) {
        this.conversationRepo = conversationRepo;
        this.messageRepo      = messageRepo;
        this.suggestionRepo   = suggestionRepo;
        this.botService       = botService;
    }

    // ── Démarrer une nouvelle conversation ────────────────────────────────────
    public MessageResponseDTO demarrerConversation(NouvelleConversationDTO dto) {
        UUID userId = dto.getUtilisateurId() != null
                ? dto.getUtilisateurId()
                : UUID.fromString("00000000-0000-0000-0000-000000000001");

        ChatConversation conv = ChatConversation.builder()
                .utilisateurId(userId)
                .utilisateurNom(dto.getUtilisateurNom() != null ? dto.getUtilisateurNom() : "Utilisateur")
                .statut(StatutConversation.EN_COURS)
                .problemeResume(dto.getMessageInitial())
                .build();
        conv = conversationRepo.save(conv);
        log.info("Nouvelle conversation créée - ID: {}", conv.getId());

        // Save the user's first message
        sauvegarderMessage(conv, Expediteur.USER, dto.getMessageInitial());

        // Search for similar incidents to enrich the AI's first response
        List<SuggestionDTO> suggestions =
                botService.rechercherIncidentsSimilaires(dto.getMessageInitial());

        // Persist suggestions so we can track them
        final ChatConversation convFinal = conv;
        suggestions.forEach(s -> {
            Suggestion suggestion = Suggestion.builder()
                    .conversation(convFinal)
                    .incidentSimilaireId(s.getIncidentSimilaireId())
                    .titreIncident(s.getTitreIncident())
                    .solutionIncident(s.getSolutionIncident())
                    .scoreSimilarite(s.getScoreSimilarite())
                    .accepte(false)
                    .build();
            Suggestion saved = suggestionRepo.save(suggestion);
            s.setId(saved.getId());
        });

        // AI responds with full context — no history yet on first message
        String rawAiResponse = botService.repondreAvecIA(
                dto.getMessageInitial(), new ArrayList<>(), suggestions);

        // Check if AI already wants to take a terminal action on the first message
        String botText;
        String action;
        if (botService.aiDitResolu(rawAiResponse)) {
            conv.setStatut(StatutConversation.RESOLU);
            conversationRepo.save(conv);
            botText = botService.nettoyerReponse(rawAiResponse);
            action = "RESOLU";
        } else if (botService.aiDitCreerTicket(rawAiResponse)) {
            Map<String, Object> incident = botService.creerIncident(
                    conv.getProblemeResume(),
                    "Problème signalé par chat: " + conv.getProblemeResume(),
                    conv.getUtilisateurNom());
            String ref = incident.getOrDefault("reference",
                    "INC-" + System.currentTimeMillis() % 100000).toString();
            UUID incidentId = incident.containsKey("id")
                    ? UUID.fromString(incident.get("id").toString()) : UUID.randomUUID();
            conv.setStatut(StatutConversation.INCIDENT_CREE);
            conv.setIncidentCreerId(incidentId);
            conversationRepo.save(conv);
            botText = botService.messageCreationTicket(ref);
            action = "INCIDENT_CREE";
        } else {
            botText = botService.nettoyerReponse(rawAiResponse);
            action = suggestions.isEmpty() ? "ATTENDRE_PROBLEME" : "FEEDBACK_SOLUTION";
        }

        ChatMessage msgBot = sauvegarderMessage(conv, Expediteur.BOT, botText);
        return buildResponse(msgBot, conv, botText, suggestions, action);
    }

    // ── Envoyer un message dans une conversation existante ────────────────────
    public MessageResponseDTO envoyerMessage(MessageDTO dto) {
        ChatConversation conv = conversationRepo.findById(dto.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation introuvable: " + dto.getConversationId()));

        if (conv.getStatut() != StatutConversation.EN_COURS) {
            throw new IllegalStateException(
                    "Cette conversation est terminée (statut: " + conv.getStatut() + ")");
        }

        // Save the user's message
        sauvegarderMessage(conv, Expediteur.USER, dto.getMessage());

        // ── Build conversation history from DB (so the AI has full context) ───
        List<Map<String, String>> history =
                messageRepo.findByConversationIdOrderByTimestampAsc(conv.getId())
                        .stream()
                        // Exclude the message we just saved (it's passed as newMessage below)
                        .filter(m -> !m.getMessage().equals(dto.getMessage())
                                || m.getExpediteur() != Expediteur.USER)
                        .map(m -> Map.of(
                                "role",    m.getExpediteur() == Expediteur.USER ? "user" : "model",
                                "content", m.getMessage()))
                        .collect(Collectors.toList());

        // ── Search for new similar incidents relevant to this message ─────────
        List<SuggestionDTO> suggestions = new ArrayList<>();
        // Only search again if the conversation doesn't already have unresolved suggestions
        List<Suggestion> existingSuggestions = suggestionRepo.findByConversationId(conv.getId())
                .stream().filter(s -> !s.getAccepte()).collect(Collectors.toList());

        if (existingSuggestions.isEmpty()) {
            List<SuggestionDTO> nouvelles =
                    botService.rechercherIncidentsSimilaires(dto.getMessage());
            final ChatConversation convFinal = conv;
            nouvelles.forEach(s -> {
                Suggestion suggestion = Suggestion.builder()
                        .conversation(convFinal)
                        .incidentSimilaireId(s.getIncidentSimilaireId())
                        .titreIncident(s.getTitreIncident())
                        .solutionIncident(s.getSolutionIncident())
                        .scoreSimilarite(s.getScoreSimilarite())
                        .accepte(false)
                        .build();
                Suggestion saved = suggestionRepo.save(suggestion);
                s.setId(saved.getId());
            });
            suggestions = nouvelles;
        }

        // ── Let the AI respond with full history ──────────────────────────────
        String rawAiResponse = botService.repondreAvecIA(dto.getMessage(), history, suggestions);

        // ── Detect terminal actions from AI's own tags ────────────────────────
        String botText;
        String action;

        if (botService.aiDitResolu(rawAiResponse)) {
            // Mark all pending suggestions as accepted
            existingSuggestions.forEach(s -> {
                s.setAccepte(true);
                suggestionRepo.save(s);
            });
            conv.setStatut(StatutConversation.RESOLU);
            conversationRepo.save(conv);
            botText = botService.messageResolu();
            action = "RESOLU";

        } else if (botService.aiDitCreerTicket(rawAiResponse)) {
            Map<String, Object> incident = botService.creerIncident(
                    conv.getProblemeResume(),
                    "Problème signalé par chat: " + conv.getProblemeResume()
                            + "\n\nDernier message: " + dto.getMessage(),
                    conv.getUtilisateurNom());
            String ref = incident.getOrDefault("reference",
                    "INC-" + System.currentTimeMillis() % 100000).toString();
            UUID incidentId = incident.containsKey("id")
                    ? UUID.fromString(incident.get("id").toString()) : UUID.randomUUID();
            conv.setStatut(StatutConversation.INCIDENT_CREE);
            conv.setIncidentCreerId(incidentId);
            conversationRepo.save(conv);
            botText = botService.messageCreationTicket(ref);
            action = "INCIDENT_CREE";

        } else {
            // AI is still having a normal conversation
            botText = botService.nettoyerReponse(rawAiResponse);
            action = suggestions.isEmpty() ? "FEEDBACK_SOLUTION" : "FEEDBACK_SOLUTION";
        }

        ChatMessage msgBot = sauvegarderMessage(conv, Expediteur.BOT, botText);
        return buildResponse(msgBot, conv, botText, suggestions, action);
    }

    // ── Obtenir une conversation avec son historique ───────────────────────────
    @Transactional(readOnly = true)
    public ConversationDTO obtenirConversation(UUID conversationId) {
        ChatConversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation introuvable: " + conversationId));

        List<MessageResponseDTO> messages = conv.getMessages().stream()
                .map(m -> MessageResponseDTO.builder()
                        .id(m.getId())
                        .conversationId(conv.getId())
                        .expediteur(m.getExpediteur())
                        .message(m.getMessage())
                        .timestamp(m.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ConversationDTO.builder()
                .id(conv.getId())
                .utilisateurId(conv.getUtilisateurId())
                .utilisateurNom(conv.getUtilisateurNom())
                .dateCreation(conv.getDateCreation())
                .statut(conv.getStatut())
                .problemeResume(conv.getProblemeResume())
                .incidentCreerId(conv.getIncidentCreerId())
                .messages(messages)
                .build();
    }

    // ── Lister les conversations d'un utilisateur ──────────────────────────────
    @Transactional(readOnly = true)
    public List<ConversationDTO> listerConversations(UUID utilisateurId) {
        return conversationRepo
                .findByUtilisateurIdOrderByDateCreationDesc(utilisateurId)
                .stream()
                .map(conv -> ConversationDTO.builder()
                        .id(conv.getId())
                        .utilisateurId(conv.getUtilisateurId())
                        .utilisateurNom(conv.getUtilisateurNom())
                        .dateCreation(conv.getDateCreation())
                        .statut(conv.getStatut())
                        .problemeResume(conv.getProblemeResume())
                        .incidentCreerId(conv.getIncidentCreerId())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private ChatMessage sauvegarderMessage(ChatConversation conv,
                                           Expediteur expediteur,
                                           String texte) {
        ChatMessage msg = ChatMessage.builder()
                .conversation(conv)
                .expediteur(expediteur)
                .message(texte)
                .build();
        return messageRepo.save(msg);
    }

    private MessageResponseDTO buildResponse(ChatMessage msg,
                                             ChatConversation conv,
                                             String text,
                                             List<SuggestionDTO> suggestions,
                                             String action) {
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .conversationId(conv.getId())
                .expediteur(Expediteur.BOT)
                .message(text)
                .timestamp(msg.getTimestamp())
                .suggestions(suggestions)
                .actionRequise(action)
                .build();
    }
}
