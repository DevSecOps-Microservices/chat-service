package com.gestion.incidents.chatservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final ChatClient chatClient;

    public OllamaService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // Multi-turn: same signature as GeminiService.ask()
    public String ask(String systemPrompt, List<Map<String, String>> history, String newMessage) {
        // Build history as a single context block
        StringBuilder fullPrompt = new StringBuilder();
        for (Map<String, String> turn : history) {
            String role = turn.get("role").equals("user") ? "User" : "Assistant";
            fullPrompt.append(role).append(": ").append(turn.get("content")).append("\n");
        }
        fullPrompt.append("User: ").append(newMessage);

        try {
            log.info("Appel Ollama - turns: {}", history.size() + 1);

            String result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(fullPrompt.toString())
                    .call()
                    .content();

            log.info("Ollama a répondu ({} caractères)", result.length());
            return result;

        } catch (Exception e) {
            log.error("Erreur Ollama: {}", e.getMessage());
            return null;
        }
    }

    // Single-turn: backward compatible
    public String ask(String systemContext, String userMessage) {
        return ask(systemContext, List.of(), userMessage);
    }
}
