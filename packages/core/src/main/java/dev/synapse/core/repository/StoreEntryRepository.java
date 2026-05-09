package dev.synapse.core.repository;

import dev.synapse.core.domain.StoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreEntryRepository extends JpaRepository<StoreEntry, String> {
    List<StoreEntry> findByType(StoreEntry.StoreEntryType type);
    List<StoreEntry> findBySource(String source);
}
