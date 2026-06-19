package com.gestion.incidents.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class NouvelleConversationDTO {
    private UUID utilisateurId;
    private String utilisateurNom;

    @NotBlank(message = "Le message initial est obligatoire")
    private String messageInitial;

    public NouvelleConversationDTO() {}
    public UUID getUtilisateurId() { return utilisateurId; }
    public String getUtilisateurNom() { return utilisateurNom; }
    public String getMessageInitial() { return messageInitial; }
    public void setUtilisateurId(UUID v) { this.utilisateurId = v; }
    public void setUtilisateurNom(String v) { this.utilisateurNom = v; }
    public void setMessageInitial(String v) { this.messageInitial = v; }
}
