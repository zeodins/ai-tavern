package com.aitavern.service;

import com.aitavern.entity.UserPersona;
import com.aitavern.repository.UserPersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PersonaService {

    private final UserPersonaRepository repo;

    public PersonaService(UserPersonaRepository repo) {
        this.repo = repo;
    }

    public List<UserPersona> list() {
        return repo.findAll();
    }

    public UserPersona getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("User persona not found: " + id));
    }

    public UserPersona create(UserPersona persona) {
        return repo.save(persona);
    }

    public UserPersona update(Long id, UserPersona updated) {
        UserPersona existing = getById(id);
        existing.setName(updated.getName());
        existing.setContent(updated.getContent());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public UserPersona getActive() {
        return repo.findByIsActiveTrue().orElse(null);
    }

    @Transactional
    public void activate(Long id) {
        repo.findAll().forEach(p -> p.setIsActive(false));
        UserPersona target = getById(id);
        target.setIsActive(true);
        repo.save(target);
    }
}
