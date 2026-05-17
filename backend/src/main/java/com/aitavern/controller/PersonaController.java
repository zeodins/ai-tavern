package com.aitavern.controller;

import com.aitavern.entity.UserPersona;
import com.aitavern.service.PersonaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaService service;

    public PersonaController(PersonaService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserPersona> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public UserPersona getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public UserPersona create(@RequestBody UserPersona persona) {
        return service.create(persona);
    }

    @PutMapping("/{id}")
    public UserPersona update(@PathVariable Long id, @RequestBody UserPersona persona) {
        return service.update(id, persona);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
