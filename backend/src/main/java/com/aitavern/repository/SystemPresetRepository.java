package com.aitavern.repository;

import com.aitavern.entity.SystemPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemPresetRepository extends JpaRepository<SystemPreset, Long> {
    List<SystemPreset> findByIsActiveTrue();
}
