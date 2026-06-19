package com.gestion.incidents.chatservice.service;

import com.gestion.incidents.chatservice.client.IncidentClient;
import com.gestion.incidents.chatservice.dto.SuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatBotService {

    private static final Logger log = LoggerFactory.getLogger(ChatBotService.class);

    private final IncidentClient incidentClient;
    private final OllamaService ollamaService;

    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${chat.bot.name:Assistant IT}")
    private String botName;

    // ─────────────────────────────────────────────────────────────────────────
    // SYSTEM PROMPT — This is the only "if/else" that matters.
    // The AI handles all conversation logic; we only intercept explicit actions.
    // ─────────────────────────────────────────────────────────────────────────
    static final String SYSTEM_PROMPT = "Tu es un assistant IT pour la gestion des incidents informatiques.\n" +
            "Tu reponds UNIQUEMENT en francais, de facon concise et professionnelle.\n" +
            "Tu utilises des emojis pour rendre tes messages agreables.\n\n" +
            "REGLES STRICTES:\n" +
            "- Tu ne dois JAMAIS inventer des donnees, des incidents, des noms ou des references.\n" +
            "- Tu travailles UNIQUEMENT avec les informations fournies dans le contexte.\n" +
            "- Si les incidents fournis ne correspondent PAS au probleme de l'utilisateur, dis-le clairement.\n" +
            "- Reponds exactement : 'Nous n'avons aucun incident similaire a votre probleme dans notre base de donnees.'\n" +
            "- Dans ce cas, propose de creer un ticket de support.\n" +
            "- Si un incident correspond, explique la solution clairement.\n" +
            "- Termine en demandant si la solution a fonctionne (repondre oui ou non).";

    public ChatBotService(IncidentClient incidentClient, OllamaService ollamaService) {
        this.incidentClient = incidentClient;
        this.ollamaService = ollamaService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI response — always passes full history for context
    // ─────────────────────────────────────────────────────────────────────────

    public String repondreAvecIA(String userMessage,
                                 List<Map<String, String>> history,
                                 List<SuggestionDTO> suggestions) {
        if (!aiEnabled || ollamaService == null) { // ← CHANGED
            log.warn("IA désactivée - mode fallback texte");
            return suggestions.isEmpty()
                    ? fallbackAucuneSuggestion()
                    : fallbackAvecSuggestions(suggestions);
        }

        // Enrich the user message with any suggestions found, so the AI can reference them
        String enrichedMessage = userMessage;
        if (!suggestions.isEmpty()) {
            String suggestionsContext = suggestions.stream()
                    .map(s -> "• " + s.getTitreIncident() + " → " + s.getSolutionIncident())
                    .collect(Collectors.joining("\n"));
            enrichedMessage = userMessage
                    + "\n\n[Incidents similaires trouvés dans la base :]\n" + suggestionsContext
                    + "\n[Présente ces solutions à l'utilisateur et demande si l'une d'elles résout son problème.]";
        }

        String response = ollamaService.ask(SYSTEM_PROMPT, history, enrichedMessage); // ← CHANGED

        if (response != null) {
            log.info("Réponse Ollama générée ({} caractères)", response.length()); // ← CHANGED
            return response;
        }

        log.warn("Ollama indisponible - mode fallback"); // ← CHANGED
        return suggestions.isEmpty()
                ? fallbackAucuneSuggestion()
                : fallbackAvecSuggestions(suggestions);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Action detection — parse the AI's own tags, no more keyword matching
    // ─────────────────────────────────────────────────────────────────────────

    public boolean aiDitResolu(String aiResponse) {
        return aiResponse != null && aiResponse.contains("[ACTION:RESOLU]");
    }

    public boolean aiDitCreerTicket(String aiResponse) {
        return aiResponse != null && aiResponse.contains("[ACTION:CREER_TICKET]");
    }

    /** Strip action tags before sending message to the user */
    public String nettoyerReponse(String aiResponse) {
        if (aiResponse == null) return "";
        return aiResponse
                .replace("[ACTION:RESOLU]", "")
                .replace("[ACTION:CREER_TICKET]", "")
                .trim();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Incident operations
    // ─────────────────────────────────────────────────────────────────────────

    public List<SuggestionDTO> rechercherIncidentsSimilaires(String probleme) {
        log.info("Recherche d'incidents similaires pour: {}", probleme);
        try {
            // Fetch all incidents and filter locally by keyword
            List<Map<String, Object>> tous = incidentClient.getAllIncidents();
            String[] keywords = probleme.toLowerCase().split("\\s+");

            return tous.stream()
                    .filter(i -> {
                        String titre = i.getOrDefault("titre", "").toString().toLowerCase();
                        String desc  = i.getOrDefault("description", "").toString().toLowerCase();
                        return Arrays.stream(keywords)
                                .filter(k -> k.length() > 3) // ignore short words like "le", "un", "de"
                                .anyMatch(k -> titre.contains(k) || desc.contains(k));
                    })
                    .limit(3)
                    .map(incident -> {
                        SuggestionDTO s = new SuggestionDTO();
                        s.setIncidentSimilaireId(UUID.fromString(
                                incident.getOrDefault("id", UUID.randomUUID()).toString()));
                        s.setTitreIncident(incident.getOrDefault("titre", "Incident similaire").toString());
                        s.setSolutionIncident(incident.getOrDefault("description", "Voir les détails").toString());
                        s.setScoreSimilarite(0.85f);
                        s.setAccepte(false);
                        return s;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Service incident indisponible - suggestions démo: {}", e.getMessage());
            return generSuggestionsDemonstration(probleme);
        }
    }

    public Map<String, Object> creerIncident(String titre, String description, String utilisateurNom) {
        try {
            Map<String, Object> incident = new HashMap<>();
            incident.put("titre", titre);
            incident.put("description", description);
            incident.put("priorite", "MOYENNE");
            incident.put("statut", "NOUVEAU");
            incident.put("declarantNom", utilisateurNom);
            return incidentClient.creerIncident(incident);
        } catch (Exception e) {
            log.warn("Impossible de créer l'incident via API: {}", e.getMessage());
            Map<String, Object> mock = new HashMap<>();
            mock.put("id", UUID.randomUUID().toString());
            mock.put("reference", "INC-" + System.currentTimeMillis() % 10000);
            mock.put("statut", "NOUVEAU");
            return mock;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Static messages for terminal states (used after action tags detected)
    // ─────────────────────────────────────────────────────────────────────────

    public String messageResolu() {
        return "🎉 Excellent ! Je suis ravi que le problème soit résolu.\n\n"
                + "✅ Cette conversation est marquée comme **résolue**.\n\n"
                + "Bonne journée ! N'hésitez pas à revenir si vous avez d'autres problèmes. 😊";
    }

    public String messageCreationTicket(String referenceTicket) {
        return "📋 Ticket créé avec succès !\n\n"
                + "📌 Référence : **" + referenceTicket + "**\n"
                + "⏱️ Priorité : Moyenne\n"
                + "👨‍💻 Un technicien vous contactera dans les **2 heures**.\n\n"
                + "Merci de votre patience !";
    }

    public String messageAccueil() {
        return "👋 Bonjour ! Je suis votre " + botName + ".\n\n"
                + "Décrivez votre problème en quelques mots et je vais rechercher "
                + "des solutions existantes avant de créer un ticket.\n\n"
                + "Quel est votre problème ?";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Fallback messages (when AI is unavailable)
    // ─────────────────────────────────────────────────────────────────────────

    private String fallbackAvecSuggestions(List<SuggestionDTO> suggestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔍 J'ai trouvé ").append(suggestions.size()).append(" solution(s) similaire(s) :\n\n");
        for (int i = 0; i < suggestions.size(); i++) {
            SuggestionDTO s = suggestions.get(i);
            sb.append("📄 **Option ").append(i + 1).append("** : ").append(s.getTitreIncident()).append("\n");
            sb.append("   💡 ").append(s.getSolutionIncident()).append("\n\n");
        }
        sb.append("Une de ces solutions a-t-elle résolu votre problème ? (oui / non)");
        return sb.toString();
    }

    private String fallbackAucuneSuggestion() {
        return "🔍 Aucun cas similaire trouvé.\n\n"
                + "Souhaitez-vous que je crée un ticket ? Un technicien vous contactera dans les 2 heures. (oui / non)";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kept ONLY as last-resort fallback when AI is fully down
    // ─────────────────────────────────────────────────────────────────────────

    public boolean estConfirmationPositive(String message) {
        String msg = message.toLowerCase().trim();
        return msg.equals("oui") || msg.equals("yes") || msg.equals("ok");
    }

    public boolean estRefus(String message) {
        String msg = message.toLowerCase().trim();
        return msg.equals("non") || msg.equals("no");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo suggestions when incident-service is down
    // ─────────────────────────────────────────────────────────────────────────

    private List<SuggestionDTO> generSuggestionsDemonstration(String probleme) {
        List<SuggestionDTO> suggestions = new ArrayList<>();
        String p = probleme.toLowerCase();

        if (p.contains("imprimante") || p.contains("impression")) {
            suggestions.add(SuggestionDTO.builder()
                    .incidentSimilaireId(UUID.randomUUID())
                    .titreIncident("Imprimante HP bloquée")
                    .solutionIncident("Redémarrer le spooler : services.msc → Print Spooler → Redémarrer")
                    .scoreSimilarite(0.92f).accepte(false).build());
            suggestions.add(SuggestionDTO.builder()
                    .incidentSimilaireId(UUID.randomUUID())
                    .titreIncident("Imprimante ne répond pas")
                    .solutionIncident("Désinstaller et réinstaller les pilotes d'impression")
                    .scoreSimilarite(0.78f).accepte(false).build());
        } else if (p.contains("mot de passe") || p.contains("password") || p.contains("connexion")) {
            suggestions.add(SuggestionDTO.builder()
                    .incidentSimilaireId(UUID.randomUUID())
                    .titreIncident("Problème de connexion Active Directory")
                    .solutionIncident("Vider le cache : Panneau de configuration → Gestionnaire d'identifiants")
                    .scoreSimilarite(0.88f).accepte(false).build());
        } else if (p.contains("réseau") || p.contains("internet") || p.contains("wifi")) {
            suggestions.add(SuggestionDTO.builder()
                    .incidentSimilaireId(UUID.randomUUID())
                    .titreIncident("Perte de connexion réseau intermittente")
                    .solutionIncident("Désactiver/réactiver la carte réseau, puis : ipconfig /flushdns")
                    .scoreSimilarite(0.85f).accepte(false).build());
        } else {
            suggestions.add(SuggestionDTO.builder()
                    .incidentSimilaireId(UUID.randomUUID())
                    .titreIncident("Problème système générique")
                    .solutionIncident("Redémarrer le poste de travail et réessayer")
                    .scoreSimilarite(0.65f).accepte(false).build());
        }
        return suggestions;
    }

}
