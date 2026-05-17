package com.aitavern.controller;

import com.aitavern.entity.AvatarImage;
import com.aitavern.service.AvatarService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avatars")
public class AvatarController {

    private final AvatarService service;

    public AvatarController(AvatarService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String id) {
        return service.getById(id)
                .map(img -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, img.getContentType())
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                        .body(img.getData()))
                .orElse(ResponseEntity.notFound().build());
    }
}
