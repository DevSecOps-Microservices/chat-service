package com.gestion.incidents.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class MessageDTO {
    private UUID conversationId;

    @NotBlank(message = "Le message ne peut pas être vide")
    private String message;

    public MessageDTO() {}
    public UUID getConversationId() { return conversationId; }
    public String getMessage() { return message; }
    public void setConversationId(UUID v) { this.conversationId = v; }
    public void setMessage(String v) { this.message = v; }
}
