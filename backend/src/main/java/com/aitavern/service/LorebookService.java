package com.aitavern.service;

import com.aitavern.entity.CharacterEntity;
import com.aitavern.entity.LorebookEntry;
import com.aitavern.repository.CharacterRepository;
import com.aitavern.repository.LorebookEntryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LorebookService {

    private final LorebookEntryRepository repo;
    private final CharacterRepository characterRepo;

    public LorebookService(LorebookEntryRepository repo, CharacterRepository characterRepo) {
        this.repo = repo;
        this.characterRepo = characterRepo;
    }

    public List<LorebookEntry> listByCharacter(Long characterId) {
        return repo.findByCharacterIdAndEnabledTrue(characterId);
    }

    public LorebookEntry getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lorebook entry not found: " + id));
    }

    public LorebookEntry create(Long characterId, LorebookEntry entry) {
        CharacterEntity character = characterRepo.findById(characterId)
                .orElseThrow(() -> new RuntimeException("Character not found: " + characterId));
        entry.setCharacter(character);
        return repo.save(entry);
    }

    public LorebookEntry update(Long id, LorebookEntry updated) {
        LorebookEntry existing = getById(id);
        existing.setKeys(updated.getKeys());
        existing.setContent(updated.getContent());
        existing.setEnabled(updated.getEnabled());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<LorebookEntry> findMatching(Long characterId, String userMessage) {
        List<LorebookEntry> entries = repo.findByCharacterIdAndEnabledTrue(characterId);
        List<LorebookEntry> matched = new ArrayList<>();
        for (LorebookEntry entry : entries) {
            String[] keywords = entry.getKeys().split(",");
            for (String kw : keywords) {
                if (userMessage.toLowerCase().contains(kw.trim().toLowerCase())) {
                    matched.add(entry);
                    break;
                }
            }
        }
        return matched;
    }
}
