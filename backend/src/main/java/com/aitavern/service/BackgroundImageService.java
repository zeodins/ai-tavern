package com.aitavern.service;

import com.aitavern.entity.BackgroundImage;
import com.aitavern.repository.BackgroundImageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BackgroundImageService {

    private final BackgroundImageRepository repo;

    public BackgroundImageService(BackgroundImageRepository repo) {
        this.repo = repo;
    }

    public BackgroundImage store(byte[] data, String contentType) {
        BackgroundImage image = new BackgroundImage(data, contentType);
        return repo.save(image);
    }

    public Optional<BackgroundImage> getById(String id) {
        return repo.findById(id);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}
