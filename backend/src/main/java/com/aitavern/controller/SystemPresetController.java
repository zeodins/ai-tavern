package com.aitavern.controller;

import com.aitavern.entity.SystemPreset;
import com.aitavern.service.SystemPresetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presets")
public class SystemPresetController {

    private final SystemPresetService service;

    public SystemPresetController(SystemPresetService service) {
        this.service = service;
    }

    @GetMapping
    public List<SystemPreset> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public SystemPreset getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public SystemPreset create(@RequestBody SystemPreset preset) {
        return service.create(preset);
    }

    @PutMapping("/{id}")
    public SystemPreset update(@PathVariable Long id, @RequestBody SystemPreset preset) {
        return service.update(id, preset);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}/toggle")
    public void toggle(@PathVariable Long id) {
        service.toggleActive(id);
    }
}
