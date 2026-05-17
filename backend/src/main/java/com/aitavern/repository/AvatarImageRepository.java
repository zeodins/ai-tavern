package com.aitavern.repository;

import com.aitavern.entity.AvatarImage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvatarImageRepository extends MongoRepository<AvatarImage, String> {
}
