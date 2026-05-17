package com.aitavern.repository;

import com.aitavern.entity.BackgroundImage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BackgroundImageRepository extends MongoRepository<BackgroundImage, String> {
}
