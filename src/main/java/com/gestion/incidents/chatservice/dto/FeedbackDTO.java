package com.gestion.incidents.chatservice.dto;

import java.util.UUID;

public class FeedbackDTO {
    private UUID conversationId;
    private UUID suggestionId;    // null si aucune suggestion ne fonctionne
    private Boolean solutionFonctionne;
    private String commentaire;

    public FeedbackDTO() {}
    public UUID getConversationId() { return conversationId; }
    public UUID getSuggestionId() { return suggestionId; }
    public Boolean getSolutionFonctionne() { return solutionFonctionne; }
    public String getCommentaire() { return commentaire; }
    public void setConversationId(UUID v) { this.conversationId = v; }
    public void setSuggestionId(UUID v) { this.suggestionId = v; }
    public void setSolutionFonctionne(Boolean v) { this.solutionFonctionne = v; }
    public void setCommentaire(String v) { this.commentaire = v; }
}
