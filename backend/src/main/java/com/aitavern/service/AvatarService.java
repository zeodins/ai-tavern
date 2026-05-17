package com.aitavern.service;

import com.aitavern.entity.AvatarImage;
import com.aitavern.repository.AvatarImageRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AvatarService {

    private final AvatarImageRepository repo;

    public AvatarService(AvatarImageRepository repo) {
        this.repo = repo;
    }

    public AvatarImage store(byte[] data, String contentType) {
        AvatarImage image = new AvatarImage(data, contentType);
        return repo.save(image);
    }

    public Optional<AvatarImage> getById(String id) {
        return repo.findById(id);
    }

    public void delete(String id) {
        repo.deleteById(id);
    }
}
