package com.aitavern.controller;

import com.aitavern.entity.LorebookEntry;
import com.aitavern.service.LorebookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LorebookEntryController {

    private final LorebookService service;

    public LorebookEntryController(LorebookService service) {
        this.service = service;
    }

    @GetMapping("/api/characters/{characterId}/lorebook")
    public List<LorebookEntry> listByCharacter(@PathVariable Long characterId) {
        return service.listByCharacter(characterId);
    }

    @PostMapping("/api/characters/{characterId}/lorebook")
    public LorebookEntry create(@PathVariable Long characterId, @RequestBody LorebookEntry entry) {
        return service.create(characterId, entry);
    }

    @PutMapping("/api/lorebook/{id}")
    public LorebookEntry update(@PathVariable Long id, @RequestBody LorebookEntry entry) {
        return service.update(id, entry);
    }

    @DeleteMapping("/api/lorebook/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
