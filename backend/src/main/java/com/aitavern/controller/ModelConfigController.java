package com.aitavern.controller;

import com.aitavern.entity.ModelConfig;
import com.aitavern.service.ModelConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelConfigController {

    private final ModelConfigService service;

    public ModelConfigController(ModelConfigService service) {
        this.service = service;
    }

    @GetMapping
    public List<ModelConfig> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ModelConfig getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public ModelConfig create(@RequestBody ModelConfig config) {
        return service.create(config);
    }

    @PutMapping("/{id}")
    public ModelConfig update(@PathVariable Long id, @RequestBody ModelConfig config) {
        return service.update(id, config);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        service.activate(id);
    }
}
