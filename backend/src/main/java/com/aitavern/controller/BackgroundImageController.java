package com.aitavern.controller;

import com.aitavern.entity.BackgroundImage;
import com.aitavern.service.BackgroundImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backgrounds")
public class BackgroundImageController {

    private final BackgroundImageService service;

    public BackgroundImageController(BackgroundImageService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getBackground(@PathVariable String id) {
        return service.getById(id)
                .map(img -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, img.getContentType())
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                        .body(img.getData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
