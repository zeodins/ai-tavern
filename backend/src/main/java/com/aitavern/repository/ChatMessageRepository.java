package com.aitavern.repository;

import com.aitavern.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCharacterIdOrderByCreatedAtAsc(Long characterId);
    void deleteByCharacterId(Long characterId);
    Optional<ChatMessage> findTopByCharacter_IdAndRoleOrderByCreatedAtDesc(Long characterId, String role);
}
