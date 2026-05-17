package com.aitavern.repository;

import com.aitavern.entity.UserPersona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPersonaRepository extends JpaRepository<UserPersona, Long> {
    Optional<UserPersona> findFirstByOrderByCreatedAtDesc();
}
