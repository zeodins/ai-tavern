package com.aitavern.controller;

import com.aitavern.entity.CharacterEntity;
import com.aitavern.service.CharacterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService service;

    public CharacterController(CharacterService service) {
        this.service = service;
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
}
