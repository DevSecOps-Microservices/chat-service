package com.gestion.incidents.chatservice.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "suggestions")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Column(name = "incident_similaire_id")
    private UUID incidentSimilaireId;

    @Column(name = "titre_incident")
    private String titreIncident;

    @Column(name = "solution_incident", columnDefinition = "TEXT")
    private String solutionIncident;

    @Column(name = "score_similarite")
    private Float scoreSimilarite;

    @Column(name = "accepte")
    private Boolean accepte = false;

    public Suggestion() {}

    public UUID getId() { return id; }
    public ChatConversation getConversation() { return conversation; }
    public UUID getIncidentSimilaireId() { return incidentSimilaireId; }
    public String getTitreIncident() { return titreIncident; }
    public String getSolutionIncident() { return solutionIncident; }
    public Float getScoreSimilarite() { return scoreSimilarite; }
    public Boolean getAccepte() { return accepte; }

    public void setId(UUID id) { this.id = id; }
    public void setConversation(ChatConversation v) { this.conversation = v; }
    public void setIncidentSimilaireId(UUID v) { this.incidentSimilaireId = v; }
    public void setTitreIncident(String v) { this.titreIncident = v; }
    public void setSolutionIncident(String v) { this.solutionIncident = v; }
    public void setScoreSimilarite(Float v) { this.scoreSimilarite = v; }
    public void setAccepte(Boolean v) { this.accepte = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final Suggestion o = new Suggestion();
        public Builder conversation(ChatConversation v) { o.conversation = v; return this; }
        public Builder incidentSimilaireId(UUID v) { o.incidentSimilaireId = v; return this; }
        public Builder titreIncident(String v) { o.titreIncident = v; return this; }
        public Builder solutionIncident(String v) { o.solutionIncident = v; return this; }
        public Builder scoreSimilarite(Float v) { o.scoreSimilarite = v; return this; }
        public Builder accepte(Boolean v) { o.accepte = v; return this; }
        public Suggestion build() { return o; }
    }
}
