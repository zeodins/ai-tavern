package com.aitavern.repository;

import com.aitavern.entity.LorebookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LorebookEntryRepository extends JpaRepository<LorebookEntry, Long> {
    List<LorebookEntry> findByCharacterIdAndEnabledTrue(Long characterId);
}
