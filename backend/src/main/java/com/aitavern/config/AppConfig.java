package com.aitavern.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() throws Exception {
        Files.createDirectories(Path.of(uploadDir));
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
