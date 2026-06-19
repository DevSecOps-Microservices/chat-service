package com.gestion.incidents.chatservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Expediteur expediteur;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public UUID getId() { return id; }
    public ChatConversation getConversation() { return conversation; }
    public Expediteur getExpediteur() { return expediteur; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setId(UUID id) { this.id = id; }
    public void setConversation(ChatConversation v) { this.conversation = v; }
    public void setExpediteur(Expediteur v) { this.expediteur = v; }
    public void setMessage(String v) { this.message = v; }
    public void setTimestamp(LocalDateTime v) { this.timestamp = v; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final ChatMessage o = new ChatMessage();
        public Builder conversation(ChatConversation v) { o.conversation = v; return this; }
        public Builder expediteur(Expediteur v) { o.expediteur = v; return this; }
        public Builder message(String v) { o.message = v; return this; }
        public ChatMessage build() { return o; }
    }
}
