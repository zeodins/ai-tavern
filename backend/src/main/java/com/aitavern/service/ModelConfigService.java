package com.aitavern.service;

import com.aitavern.entity.ModelConfig;
import com.aitavern.repository.ModelConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModelConfigService {

    private final ModelConfigRepository repo;

    public ModelConfigService(ModelConfigRepository repo) {
        this.repo = repo;
    }

    public List<ModelConfig> list() {
        return repo.findAll();
    }

    public ModelConfig getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Model config not found: " + id));
    }

    public ModelConfig create(ModelConfig config) {
        return repo.save(config);
    }

    public ModelConfig update(Long id, ModelConfig updated) {
        ModelConfig existing = getById(id);
        existing.setName(updated.getName());
        existing.setApiBaseUrl(updated.getApiBaseUrl());
        existing.setApiKey(updated.getApiKey());
        existing.setModelName(updated.getModelName());
        existing.setTemperature(updated.getTemperature());
        existing.setMaxTokens(updated.getMaxTokens());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public void activate(Long id) {
        repo.findAll().forEach(m -> m.setIsActive(false));
        ModelConfig target = getById(id);
        target.setIsActive(true);
        repo.save(target);
    }
}
