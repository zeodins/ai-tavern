package com.aitavern.repository;

import com.aitavern.entity.ModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModelConfigRepository extends JpaRepository<ModelConfig, Long> {
    Optional<ModelConfig> findByIsActiveTrue();
}
