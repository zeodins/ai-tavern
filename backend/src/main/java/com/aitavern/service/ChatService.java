package com.aitavern.service;

import com.aitavern.entity.CharacterEntity;
import com.aitavern.entity.ChatMessage;
import com.aitavern.entity.LorebookEntry;
import com.aitavern.entity.ModelConfig;
import com.aitavern.entity.UserPersona;
import com.aitavern.repository.CharacterRepository;
import com.aitavern.repository.ChatMessageRepository;
import com.aitavern.repository.ModelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
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
import java.util.Optional;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ChatService {

    private final CharacterRepository characterRepo;
    private final ModelConfigRepository modelRepo;
    private final ChatMessageRepository messageRepo;
    private final PersonaService personaService;
    private final LorebookService lorebookService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final RedisTemplate<String, Object> redisTemplate;

    public ChatService(CharacterRepository characterRepo, ModelConfigRepository modelRepo,
                       ChatMessageRepository messageRepo, PersonaService personaService,
                       LorebookService lorebookService, RedisTemplate<String, Object> redisTemplate) {
        this.characterRepo = characterRepo;
        this.modelRepo = modelRepo;
        this.messageRepo = messageRepo;
        this.personaService = personaService;
        this.lorebookService = lorebookService;
        this.redisTemplate = redisTemplate;
    }

    public List<ChatMessage> getHistory(Long characterId) {
        String key = "chat:history:" + characterId;
        try {
            @SuppressWarnings("unchecked")
            List<ChatMessage> cached = (List<ChatMessage>) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            // Redis unavailable, fall through to DB
        }

        List<ChatMessage> history = messageRepo.findByCharacterIdOrderByCreatedAtAsc(characterId);

        try {
            redisTemplate.opsForValue().set(key, history, Duration.ofMinutes(10));
        } catch (Exception e) {
            // Redis unavailable, skip caching
        }

        return history;
    }

    public void clearHistory(Long characterId) {
        messageRepo.deleteByCharacterId(characterId);
        try {
            redisTemplate.delete("chat:history:" + characterId);
        } catch (Exception ignored) {}
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

        executor.execute(() -> {
            try {
                List<Map<String, String>> messages = buildMessages(character);

                // Inject lorebook entries matching user message
                List<LorebookEntry> matched = lorebookService.findMatching(character.getId(), userMessage);
                if (!matched.isEmpty()) {
                    StringBuilder loreContext = new StringBuilder("[Relevant world knowledge]\n");
                    for (LorebookEntry entry : matched) {
                        loreContext.append("- ").append(entry.getContent()).append("\n");
                    }
                    messages.add(Map.of("role", "system", "content", loreContext.toString()));
                }

                // Inject author's note at ~70% depth
                if (character.getAuthorNote() != null && !character.getAuthorNote().isEmpty()) {
                    int depthIdx = (int)(messages.size() * 0.7);
                    Map<String, String> note = Map.of("role", "system", "content", "[Author's Note: " + character.getAuthorNote() + "]");
                    if (depthIdx < messages.size()) {
                        messages.add(depthIdx, note);
                    } else {
                        messages.add(note);
                    }
                }

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
                    messageRepo.save(userMsg);
                    // Invalidate Redis cache
                    try {
                        redisTemplate.delete("chat:history:" + character.getId());
                    } catch (Exception ignored) {}
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

    public SseEmitter regenerate(Long characterId, Long modelConfigId) {
        // Find and delete last assistant message
        Optional<ChatMessage> lastAssistant = messageRepo.findTopByCharacter_IdAndRoleOrderByCreatedAtDesc(characterId, "assistant");
        if (lastAssistant.isPresent()) {
            messageRepo.delete(lastAssistant.get());
        }

        // Find last user message
        Optional<ChatMessage> lastUser = messageRepo.findTopByCharacter_IdAndRoleOrderByCreatedAtDesc(characterId, "user");
        if (lastUser.isEmpty()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("No user message to regenerate from"));
            return emitter;
        }

        // Reuse the chat method, which already includes lorebook/authorNote injection
        return chat(characterId, lastUser.get().getContent(), modelConfigId);
    }

    public List<String> suggest(Long characterId) {
        CharacterEntity character = characterRepo.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        ModelConfig modelConfig = character.getModelConfig();
        if (modelConfig == null) {
            return List.of("请先为角色配置模型", "然后再获取建议", "在模型管理中添加配置");
        }

        try {
            List<Map<String, String>> messages = buildMessages(character);
            // Add the last user message if it exists
            Optional<ChatMessage> lastUser = messageRepo.findTopByCharacter_IdAndRoleOrderByCreatedAtDesc(characterId, "user");
            if (lastUser.isPresent()) {
                messages.add(Map.of("role", "user", "content", lastUser.get().getContent()));
            }
            messages.add(Map.of("role", "system", "content",
                "Based on the conversation context, generate 3 short, distinct reply suggestions (under 30 characters each) that the user could send next. Return ONLY the 3 suggestions, each on a new line starting with '- '."));

            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", modelConfig.getModelName(),
                "temperature", 0.9,
                "max_tokens", 200,
                "stream", false,
                "messages", messages
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(modelConfig.getApiBaseUrl() + "/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + modelConfig.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return List.of("获取建议失败", "请检查模型配置", "或网络连接");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.get("choices").get(0).get("message").get("content").asText();

            // Parse: each suggestion starts with "- "
            List<String> suggestions = new ArrayList<>();
            for (String line : text.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("- ")) {
                    String suggestion = trimmed.substring(2).trim();
                    if (suggestion.length() > 60) {
                        suggestion = suggestion.substring(0, 60);
                    }
                    if (!suggestion.isEmpty()) {
                        suggestions.add(suggestion);
                    }
                }
            }

            return suggestions.isEmpty() ? List.of("获取建议失败", "请重试", "或检查模型配置") : suggestions;
        } catch (Exception e) {
            return List.of("获取建议失败", e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 30)) : "未知错误", "请重试");
        }
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

        // Inject active user persona
        UserPersona persona = personaService.getActive();
        if (persona != null && persona.getContent() != null && !persona.getContent().isEmpty()) {
            if (messages.isEmpty()) {
                messages.add(Map.of("role", "system", "content", "[User Persona]\nYou are: " + persona.getContent()));
            } else {
                messages.set(0, Map.of("role", "system", "content",
                    messages.get(0).get("content") + "\n\n[User Persona]\nYou are: " + persona.getContent()));
            }
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
