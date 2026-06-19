package com.gestion.incidents.chatservice.dto;

import java.util.UUID;

public class SuggestionDTO {
    private UUID id;
    private UUID incidentSimilaireId;
    private String titreIncident;
    private String solutionIncident;
    private Float scoreSimilarite;
    private Boolean accepte;

    public SuggestionDTO() {}
    public UUID getId() { return id; }
    public UUID getIncidentSimilaireId() { return incidentSimilaireId; }
    public String getTitreIncident() { return titreIncident; }
    public String getSolutionIncident() { return solutionIncident; }
    public Float getScoreSimilarite() { return scoreSimilarite; }
    public Boolean getAccepte() { return accepte; }
    public void setId(UUID v) { this.id = v; }
    public void setIncidentSimilaireId(UUID v) { this.incidentSimilaireId = v; }
    public void setTitreIncident(String v) { this.titreIncident = v; }
    public void setSolutionIncident(String v) { this.solutionIncident = v; }
    public void setScoreSimilarite(Float v) { this.scoreSimilarite = v; }
    public void setAccepte(Boolean v) { this.accepte = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final SuggestionDTO o = new SuggestionDTO();
        public Builder id(UUID v) { o.id = v; return this; }
        public Builder incidentSimilaireId(UUID v) { o.incidentSimilaireId = v; return this; }
        public Builder titreIncident(String v) { o.titreIncident = v; return this; }
        public Builder solutionIncident(String v) { o.solutionIncident = v; return this; }
        public Builder scoreSimilarite(Float v) { o.scoreSimilarite = v; return this; }
        public Builder accepte(Boolean v) { o.accepte = v; return this; }
        public SuggestionDTO build() { return o; }
    }
}
