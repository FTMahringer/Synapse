package dev.synapse.core.service;

import dev.synapse.core.domain.AgentMemoryEntry;
import dev.synapse.core.logging.LogCategory;
import dev.synapse.core.logging.LogLevel;
import dev.synapse.core.logging.SystemLogService;
import dev.synapse.core.repository.AgentMemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public AgentMemoryService(AgentMemoryRepository memoryRepository, SystemLogService logService) {
        this.memoryRepository = memoryRepository;
        this.logService = logService;
    }

    /**
     * Read a memory entry for an agent. Returns empty if not found.
     */
    @Transactional(readOnly = true)
    public Optional<AgentMemoryEntry> read(String agentId, String key) {
        return memoryRepository.findByAgentIdAndKey(agentId, key);
    }

    /**
     * Read all memory entries for an agent.
     */
    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> readAll(String agentId) {
        return memoryRepository.findByAgentIdOrderByKeyAsc(agentId);
    }

    /**
     * Read all memory entries for an agent in a specific namespace.
     */
    @Transactional(readOnly = true)
    public List<AgentMemoryEntry> readByNamespace(String agentId, String namespace) {
        return memoryRepository.findByAgentIdAndNamespaceOrderByKeyAsc(agentId, namespace);
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
}
