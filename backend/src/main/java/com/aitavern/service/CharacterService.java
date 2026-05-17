package com.aitavern.service;

import com.aitavern.config.AppConfig;
import com.aitavern.entity.AvatarImage;
import com.aitavern.entity.CharacterEntity;
import com.aitavern.entity.ModelConfig;
import com.aitavern.repository.CharacterRepository;
import com.aitavern.repository.ModelConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CharacterService {

    private final CharacterRepository repo;
    private final ModelConfigRepository modelRepo;
    private final CharacterCardParser parser;
    private final AppConfig appConfig;
    private final AvatarService avatarService;

    public CharacterService(CharacterRepository repo, ModelConfigRepository modelRepo,
                            CharacterCardParser parser, AppConfig appConfig,
                            AvatarService avatarService) {
        this.repo = repo;
        this.modelRepo = modelRepo;
        this.parser = parser;
        this.appConfig = appConfig;
        this.avatarService = avatarService;
    }

    public List<CharacterEntity> list() {
        return repo.findAll();
    }

    public CharacterEntity getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Character not found: " + id));
    }

    public CharacterEntity importFromFile(MultipartFile file, Long modelConfigId) throws IOException {
        var result = parser.parse(file.getInputStream(), file.getOriginalFilename());
        CharacterEntity character = new CharacterEntity();
        Map<String, String> f = result.fields();
        character.setName(f.get("name"));
        character.setDescription(f.get("description"));
        character.setPersonality(f.get("personality"));
        character.setFirstMessage(f.get("first_message"));
        character.setScenario(f.get("scenario"));
        character.setSystemPrompt(f.get("system_prompt"));
        character.setMesExample(f.get("mes_example"));

        if (result.avatarBytes() != null) {
            AvatarImage avatar = avatarService.store(result.avatarBytes(), "image/png");
            character.setAvatarId(avatar.getId());
        }

        if (modelConfigId != null) {
            ModelConfig mc = modelRepo.findById(modelConfigId)
                    .orElseThrow(() -> new RuntimeException("Model config not found: " + modelConfigId));
            character.setModelConfig(mc);
        }

        return repo.save(character);
    }

    public CharacterEntity update(Long id, CharacterEntity updated) {
        CharacterEntity existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPersonality(updated.getPersonality());
        existing.setFirstMessage(updated.getFirstMessage());
        existing.setScenario(updated.getScenario());
        existing.setSystemPrompt(updated.getSystemPrompt());
        existing.setMesExample(updated.getMesExample());
        if (updated.getModelConfig() != null && updated.getModelConfig().getId() != null) {
            ModelConfig mc = modelRepo.findById(updated.getModelConfig().getId())
                    .orElseThrow(() -> new RuntimeException("Model config not found"));
            existing.setModelConfig(mc);
        }
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
