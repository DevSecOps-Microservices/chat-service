package com.gestion.incidents.chatservice.dto;

import com.gestion.incidents.chatservice.model.StatutConversation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConversationDTO {
    private UUID id;
    private UUID utilisateurId;
    private String utilisateurNom;
    private LocalDateTime dateCreation;
    private StatutConversation statut;
    private String problemeResume;
    private UUID incidentCreerId;
    private List<MessageResponseDTO> messages;

    public ConversationDTO() {}
    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public String getUtilisateurNom() { return utilisateurNom; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public StatutConversation getStatut() { return statut; }
    public String getProblemeResume() { return problemeResume; }
    public UUID getIncidentCreerId() { return incidentCreerId; }
    public List<MessageResponseDTO> getMessages() { return messages; }
    public void setId(UUID v) { this.id = v; }
    public void setUtilisateurId(UUID v) { this.utilisateurId = v; }
    public void setUtilisateurNom(String v) { this.utilisateurNom = v; }
    public void setDateCreation(LocalDateTime v) { this.dateCreation = v; }
    public void setStatut(StatutConversation v) { this.statut = v; }
    public void setProblemeResume(String v) { this.problemeResume = v; }
    public void setIncidentCreerId(UUID v) { this.incidentCreerId = v; }
    public void setMessages(List<MessageResponseDTO> v) { this.messages = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ConversationDTO o = new ConversationDTO();
        public Builder id(UUID v) { o.id = v; return this; }
        public Builder utilisateurId(UUID v) { o.utilisateurId = v; return this; }
        public Builder utilisateurNom(String v) { o.utilisateurNom = v; return this; }
        public Builder dateCreation(LocalDateTime v) { o.dateCreation = v; return this; }
        public Builder statut(StatutConversation v) { o.statut = v; return this; }
        public Builder problemeResume(String v) { o.problemeResume = v; return this; }
        public Builder incidentCreerId(UUID v) { o.incidentCreerId = v; return this; }
        public Builder messages(List<MessageResponseDTO> v) { o.messages = v; return this; }
        public ConversationDTO build() { return o; }
    }
}
