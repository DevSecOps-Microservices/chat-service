package com.gestion.incidents.chatservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_conversations")
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "utilisateur_id", nullable = false)
    private UUID utilisateurId;

    @Column(name = "utilisateur_nom")
    private String utilisateurNom;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutConversation statut = StatutConversation.EN_COURS;

    @Column(name = "probleme_resume", columnDefinition = "TEXT")
    private String problemeResume;

    @Column(name = "incident_cree_id")
    private UUID incidentCreerId;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Suggestion> suggestions = new ArrayList<>();

    public ChatConversation() {}

    public UUID getId() { return id; }
    public UUID getUtilisateurId() { return utilisateurId; }
    public String getUtilisateurNom() { return utilisateurNom; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public StatutConversation getStatut() { return statut; }
    public String getProblemeResume() { return problemeResume; }
    public UUID getIncidentCreerId() { return incidentCreerId; }
    public List<ChatMessage> getMessages() { return messages; }
    public List<Suggestion> getSuggestions() { return suggestions; }

    public void setId(UUID id) { this.id = id; }
    public void setUtilisateurId(UUID v) { this.utilisateurId = v; }
    public void setUtilisateurNom(String v) { this.utilisateurNom = v; }
    public void setDateCreation(LocalDateTime v) { this.dateCreation = v; }
    public void setStatut(StatutConversation v) { this.statut = v; }
    public void setProblemeResume(String v) { this.problemeResume = v; }
    public void setIncidentCreerId(UUID v) { this.incidentCreerId = v; }
    public void setMessages(List<ChatMessage> v) { this.messages = v; }
    public void setSuggestions(List<Suggestion> v) { this.suggestions = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ChatConversation o = new ChatConversation();
        public Builder utilisateurId(UUID v) { o.utilisateurId = v; return this; }
        public Builder utilisateurNom(String v) { o.utilisateurNom = v; return this; }
        public Builder statut(StatutConversation v) { o.statut = v; return this; }
        public Builder problemeResume(String v) { o.problemeResume = v; return this; }
        public ChatConversation build() { return o; }
    }
}
