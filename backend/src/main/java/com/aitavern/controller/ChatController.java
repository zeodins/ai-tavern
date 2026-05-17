package com.aitavern.controller;

import com.aitavern.entity.ChatMessage;
import com.aitavern.service.ChatService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping
    public SseEmitter chat(@RequestBody Map<String, Object> body) {
        Long characterId = Long.valueOf(body.get("characterId").toString());
        String message = body.get("message").toString();
        Long modelConfigId = body.containsKey("modelConfigId") && body.get("modelConfigId") != null
                ? Long.valueOf(body.get("modelConfigId").toString()) : null;
        return service.chat(characterId, message, modelConfigId);
    }

    @GetMapping("/{characterId}")
    public List<ChatMessage> getHistory(@PathVariable Long characterId) {
        return service.getHistory(characterId);
    }

    @DeleteMapping("/{characterId}")
    public void clearHistory(@PathVariable Long characterId) {
        service.clearHistory(characterId);
    }
}
