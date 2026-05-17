package com.aitavern.controller;

import com.aitavern.entity.BackgroundImage;
import com.aitavern.entity.CharacterEntity;
import com.aitavern.entity.SystemPreset;
import com.aitavern.service.BackgroundImageService;
import com.aitavern.service.CharacterService;
import com.aitavern.service.SystemPresetService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService service;
    private final BackgroundImageService backgroundImageService;
    private final SystemPresetService presetService;

    public CharacterController(CharacterService service, BackgroundImageService backgroundImageService, SystemPresetService presetService) {
        this.service = service;
        this.backgroundImageService = backgroundImageService;
        this.presetService = presetService;
    }

    @GetMapping
    public List<CharacterEntity> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public CharacterEntity getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public CharacterEntity importCharacter(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "modelConfigId", required = false) Long modelConfigId) throws IOException {
        return service.importFromFile(file, modelConfigId);
    }

    @PutMapping("/{id}")
    public CharacterEntity update(@PathVariable Long id, @RequestBody CharacterEntity character) {
        return service.update(id, character);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/apply-preset/{presetId}")
    public CharacterEntity applyPreset(@PathVariable Long id, @PathVariable Long presetId) {
        CharacterEntity character = service.getById(id);
        SystemPreset preset = presetService.getById(presetId);
        String currentPrompt = character.getSystemPrompt() != null ? character.getSystemPrompt() : "";
        if (!currentPrompt.contains(preset.getContent())) {
            character.setSystemPrompt(currentPrompt + "\n\n" + preset.getContent());
        }
        return service.update(id, character);
    }

    @PutMapping("/{id}/background")
    public CharacterEntity setBackground(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        CharacterEntity character = service.getById(id);
        BackgroundImage bg = backgroundImageService.store(file.getBytes(), file.getContentType() != null ? file.getContentType() : "image/jpeg");
        character.setBackgroundId(bg.getId());
        return service.update(id, character);
    }
}
