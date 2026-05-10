package dev.synapse.agents.service;

import dev.synapse.core.common.domain.AgentMemoryEntry;
import dev.synapse.core.common.domain.AgentMemoryEntry.MemoryTier;
import dev.synapse.core.common.domain.AgentMemoryEntry.PromotionReason;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.common.repository.AgentMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent memory vault — read/write hooks for persistent agent memory.
 * Each entry is scoped to an agent and identified by a key.
 */
@Service
public class AgentMemoryService {

    private final AgentMemoryRepository memoryRepository;
    private final SystemLogService logService;
    private final int autoPromoteReuseThreshold;

    public AgentMemoryService(
        AgentMemoryRepository memoryRepository,
        SystemLogService logService,
        @Value("${synapse.memory.auto-promote.reuse-threshold:5}") int autoPromoteReuseThreshold
    ) {
        this.memoryRepository = memoryRepository;
        this.logService = logService;
        this.autoPromoteReuseThreshold = autoPromoteReuseThreshold;
    }

    /**
     * Read a memory entry for an agent. Returns empty if not found.
     */
    @Transactional
    public Optional<AgentMemoryEntry> read(String agentId, String key) {
        Optional<AgentMemoryEntry> entry = memoryRepository.findByAgentIdAndKey(agentId, key);
        entry.ifPresent(this::trackAccessAndAutoPromote);
        return entry;
    }

    /**
     * Read all memory entries for an agent.
     */
    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> readAll(String agentId) {
        return readTierPriority(agentId, null);
    }

    /**
     * Read all memory entries for an agent in a specific namespace.
     */
    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> readByNamespace(String agentId, String namespace) {
        return readTierPriority(agentId, namespace);
    }

    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> readByTier(String agentId, String namespace, MemoryTier tier) {
        if (namespace != null && !namespace.isBlank()) {
            return memoryRepository.findByAgentIdAndNamespaceAndTierOrderByKeyAsc(agentId, namespace, tier);
        }
        return memoryRepository.findByAgentIdAndTierOrderByKeyAsc(agentId, tier);
    }

    /**
     * Write (upsert) a memory entry for an agent.
     */
    @Transactional
    public AgentMemoryEntry write(String agentId, String key, String value, String namespace) {
        AgentMemoryEntry entry = memoryRepository.findByAgentIdAndKey(agentId, key)
            .orElseGet(() -> {
                AgentMemoryEntry e = new AgentMemoryEntry();
                e.setAgentId(agentId);
                e.setKey(key);
                return e;
            });

        boolean isNew = entry.getId() == null;
        entry.setValue(value);
        entry.setNamespace(namespace);
        if (isNew) {
            entry.setTier(MemoryTier.SHORT_TERM);
            entry.setAccessCount(0);
        }

        AgentMemoryEntry saved = memoryRepository.save(entry);

        logService.log(
            LogLevel.INFO,
            LogCategory.MEMORY,
            Map.of("component", "AgentMemoryService", "agentId", agentId),
            isNew ? "MEMORY_WRITTEN" : "MEMORY_UPDATED",
            Map.of("key", key, "namespace", namespace != null ? namespace : ""),
            null,
            null
        );

        return saved;
    }

    @Transactional
    public AgentMemoryEntry promote(
        String agentId,
        String key,
        MemoryTier targetTier,
        PromotionReason reason
    ) {
        AgentMemoryEntry entry = memoryRepository.findByAgentIdAndKey(agentId, key)
            .orElseThrow(() -> new ResourceNotFoundException("AgentMemoryEntry", agentId + "/" + key));

        validateTransition(entry.getTier(), targetTier);
        entry.setTier(targetTier);
        entry.setPromotedAt(Instant.now());
        entry.setPromotionReason(reason);

        AgentMemoryEntry saved = memoryRepository.save(entry);
        logService.log(
            LogLevel.INFO,
            LogCategory.MEMORY,
            Map.of("component", "AgentMemoryService", "agentId", agentId),
            "MEMORY_PROMOTED",
            Map.of("key", key, "toTier", targetTier.name(), "reason", reason.name()),
            null,
            null
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> findByTierAndUpdatedBefore(MemoryTier tier, Instant updatedBefore) {
        return memoryRepository.findByTierAndUpdatedAtBefore(tier, updatedBefore);
    }

    /**
     * Delete a memory entry for an agent.
     */
    @Transactional
    public void delete(String agentId, String key) {
        memoryRepository.deleteByAgentIdAndKey(agentId, key);

        logService.log(
            LogLevel.INFO,
            LogCategory.MEMORY,
            Map.of("component", "AgentMemoryService", "agentId", agentId),
            "MEMORY_DELETED",
            Map.of("key", key),
            null,
            null
        );
    }

    private List<AgentMemoryEntry> readTierPriority(String agentId, String namespace) {
        List<AgentMemoryEntry> result = new ArrayList<>();
        result.addAll(readByTier(agentId, namespace, MemoryTier.SHORT_TERM));
        result.addAll(readByTier(agentId, namespace, MemoryTier.KNOWLEDGE));
        result.addAll(readByTier(agentId, namespace, MemoryTier.ARCHIVE));
        return result;
    }

    private void trackAccessAndAutoPromote(AgentMemoryEntry entry) {
        int nextCount = entry.getAccessCount() == null ? 1 : entry.getAccessCount() + 1;
        entry.setAccessCount(nextCount);
        entry.setLastAccessedAt(Instant.now());
        if (entry.getTier() == MemoryTier.SHORT_TERM && nextCount >= autoPromoteReuseThreshold) {
            entry.setTier(MemoryTier.KNOWLEDGE);
            entry.setPromotedAt(Instant.now());
            entry.setPromotionReason(PromotionReason.REUSED);
        }
        memoryRepository.save(entry);
    }

    private void validateTransition(MemoryTier currentTier, MemoryTier targetTier) {
        if (currentTier == targetTier) {
            return;
        }

        boolean valid = (currentTier == MemoryTier.SHORT_TERM && (targetTier == MemoryTier.KNOWLEDGE || targetTier == MemoryTier.ARCHIVE))
            || (currentTier == MemoryTier.KNOWLEDGE && targetTier == MemoryTier.ARCHIVE);

        if (!valid) {
            throw new ValidationException("Invalid memory tier transition: " + currentTier.name() + " -> " + targetTier.name());
        }
    }
}
