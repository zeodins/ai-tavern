package com.aitavern.service;

import com.aitavern.entity.SystemPreset;
import com.aitavern.repository.SystemPresetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemPresetService {

    private final SystemPresetRepository repo;

    public SystemPresetService(SystemPresetRepository repo) {
        this.repo = repo;
    }

    public List<SystemPreset> list() {
        return repo.findAll();
    }

    public SystemPreset getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("System preset not found: " + id));
    }

    public SystemPreset create(SystemPreset preset) {
        return repo.save(preset);
    }

    public SystemPreset update(Long id, SystemPreset updated) {
        SystemPreset existing = getById(id);
        existing.setName(updated.getName());
        existing.setContent(updated.getContent());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public void toggleActive(Long id) {
        SystemPreset p = getById(id);
        p.setIsActive(!Boolean.TRUE.equals(p.getIsActive()));
        repo.save(p);
    }

    public List<SystemPreset> getActivePresets() {
        return repo.findByIsActiveTrue();
    }
}
