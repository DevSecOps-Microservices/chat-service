package com.gestion.incidents.chatservice.dto;

import com.gestion.incidents.chatservice.model.Expediteur;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MessageResponseDTO {
    private UUID id;
    private UUID conversationId;
    private Expediteur expediteur;
    private String message;
    private LocalDateTime timestamp;
    private List<SuggestionDTO> suggestions;
    private String actionRequise; // ATTENDRE, FEEDBACK_SOLUTION, CREER_INCIDENT, RESOLU

    public MessageResponseDTO() {}
    public UUID getId() { return id; }
    public UUID getConversationId() { return conversationId; }
    public Expediteur getExpediteur() { return expediteur; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<SuggestionDTO> getSuggestions() { return suggestions; }
    public String getActionRequise() { return actionRequise; }
    public void setId(UUID v) { this.id = v; }
    public void setConversationId(UUID v) { this.conversationId = v; }
    public void setExpediteur(Expediteur v) { this.expediteur = v; }
    public void setMessage(String v) { this.message = v; }
    public void setTimestamp(LocalDateTime v) { this.timestamp = v; }
    public void setSuggestions(List<SuggestionDTO> v) { this.suggestions = v; }
    public void setActionRequise(String v) { this.actionRequise = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final MessageResponseDTO o = new MessageResponseDTO();
        public Builder id(UUID v) { o.id = v; return this; }
        public Builder conversationId(UUID v) { o.conversationId = v; return this; }
        public Builder expediteur(Expediteur v) { o.expediteur = v; return this; }
        public Builder message(String v) { o.message = v; return this; }
        public Builder timestamp(LocalDateTime v) { o.timestamp = v; return this; }
        public Builder suggestions(List<SuggestionDTO> v) { o.suggestions = v; return this; }
        public Builder actionRequise(String v) { o.actionRequise = v; return this; }
        public MessageResponseDTO build() { return o; }
    }
}
