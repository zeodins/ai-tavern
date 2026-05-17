package com.aitavern.service;

import com.aitavern.entity.CharacterEntity;
import com.aitavern.entity.ChatMessage;
import com.aitavern.entity.ModelConfig;
import com.aitavern.repository.CharacterRepository;
import com.aitavern.repository.ChatMessageRepository;
import com.aitavern.repository.ModelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ChatService {

    private final CharacterRepository characterRepo;
    private final ModelConfigRepository modelRepo;
    private final ChatMessageRepository messageRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ChatService(CharacterRepository characterRepo, ModelConfigRepository modelRepo,
                       ChatMessageRepository messageRepo) {
        this.characterRepo = characterRepo;
        this.modelRepo = modelRepo;
        this.messageRepo = messageRepo;
    }

    public List<ChatMessage> getHistory(Long characterId) {
        return messageRepo.findByCharacterIdOrderByCreatedAtAsc(characterId);
    }

    public void clearHistory(Long characterId) {
        messageRepo.deleteByCharacterId(characterId);
    }

    public SseEmitter chat(Long characterId, String userMessage, Long modelConfigId) {
        SseEmitter emitter = new SseEmitter(300_000L);

        CharacterEntity character = characterRepo.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found"));
        ModelConfig modelConfig;
        if (modelConfigId != null) {
            modelConfig = modelRepo.findById(modelConfigId)
                    .orElseThrow(() -> new RuntimeException("Model config not found"));
        } else if (character.getModelConfig() != null) {
            modelConfig = character.getModelConfig();
        } else {
            emitter.completeWithError(new RuntimeException("No model config"));
            return emitter;
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.setCharacter(character);
        userMsg.setRole("user");
        userMsg.setContent(userMessage);
        messageRepo.save(userMsg);

        executor.execute(() -> {
            try {
                List<Map<String, String>> messages = buildMessages(character);
                messages.add(Map.of("role", "user", "content", userMessage));

                String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", modelConfig.getModelName(),
                    "temperature", modelConfig.getTemperature(),
                    "max_tokens", modelConfig.getMaxTokens(),
                    "stream", true,
                    "messages", messages
                ));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(modelConfig.getApiBaseUrl() + "/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + modelConfig.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<java.io.InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    String errorBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Model API error: " + errorBody));
                    emitter.complete();
                    return;
                }

                StringBuilder fullContent = new StringBuilder();
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String dataStr = line.substring(6);
                            if ("[DONE]".equals(dataStr)) {
                                break;
                            }
                            try {
                                JsonNode node = objectMapper.readTree(dataStr);
                                JsonNode choices = node.get("choices");
                                if (choices != null && choices.isArray() && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String chunk = delta.get("content").asText();
                                        if (!chunk.isEmpty()) {
                                            fullContent.append(chunk);
                                            emitter.send(SseEmitter.event()
                                                    .name("chunk")
                                                    .data(chunk));
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }

                if (fullContent.length() > 0) {
                    ChatMessage assistantMsg = new ChatMessage();
                    assistantMsg.setCharacter(character);
                    assistantMsg.setRole("assistant");
                    assistantMsg.setContent(fullContent.toString());
                    messageRepo.save(assistantMsg);
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private List<Map<String, String>> buildMessages(CharacterEntity character) {
        List<Map<String, String>> messages = new ArrayList<>();

        StringBuilder systemPrompt = new StringBuilder();
        appendIfNotEmpty(systemPrompt, character.getSystemPrompt());
        appendIfNotEmpty(systemPrompt, "Personality: " + character.getPersonality());
        appendIfNotEmpty(systemPrompt, "Scenario: " + character.getScenario());
        if (character.getMesExample() != null && !character.getMesExample().isEmpty()) {
            appendIfNotEmpty(systemPrompt, "Example conversation:\n" + character.getMesExample());
        }

        if (systemPrompt.length() > 0) {
            messages.add(Map.of("role", "system", "content", systemPrompt.toString()));
        }

        List<ChatMessage> history = messageRepo.findByCharacterIdOrderByCreatedAtAsc(character.getId());
        int start = Math.max(0, history.size() - 40);
        for (int i = start; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        return messages;
    }

    private void appendIfNotEmpty(StringBuilder sb, String text) {
        if (text != null && !text.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(text);
        }
    }
}
