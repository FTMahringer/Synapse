package dev.synapse.agents.service;

import dev.synapse.core.common.domain.AgentMemoryEntry;
import dev.synapse.core.common.domain.AgentMemoryEntry.MemoryTier;
import dev.synapse.core.common.domain.AgentMemoryEntry.PromotionReason;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AgentMemoryLifecycleService {

    private final AgentMemoryService memoryService;
    private final SystemLogService logService;
    private final int knowledgeCompactionDays;

    public AgentMemoryLifecycleService(
        AgentMemoryService memoryService,
        SystemLogService logService,
        @Value("${synapse.memory.lifecycle.knowledge-compaction-days:30}") int knowledgeCompactionDays
    ) {
        this.memoryService = memoryService;
        this.logService = logService;
        this.knowledgeCompactionDays = knowledgeCompactionDays;
    }

    @Scheduled(cron = "${synapse.memory.lifecycle.monthly-knowledge-cron:0 0 3 1 * *}")
    @Transactional
    public void monthlyKnowledgeCompaction() {
        Instant cutoff = Instant.now().minus(knowledgeCompactionDays, ChronoUnit.DAYS);
        List<AgentMemoryEntry> staleKnowledge = memoryService.findByTierAndUpdatedBefore(MemoryTier.KNOWLEDGE, cutoff);

        int promoted = 0;
        for (AgentMemoryEntry entry : staleKnowledge) {
            memoryService.promote(
                entry.getAgentId(),
                entry.getKey(),
                MemoryTier.ARCHIVE,
                PromotionReason.CONSOLIDATED
            );
            promoted++;
        }

        logService.log(
            LogLevel.INFO,
            LogCategory.MEMORY,
            Map.of("component", "AgentMemoryLifecycleService"),
            "MEMORY_KNOWLEDGE_COMPACTION_COMPLETED",
            Map.of("cutoffDays", knowledgeCompactionDays, "promotedToArchive", promoted),
            null,
            null
        );
    }

    @Scheduled(cron = "${synapse.memory.lifecycle.bimonthly-archive-cron:0 0 3 1 */2 *}")
    @Transactional(readOnly = true)
    public void bimonthlyArchiveCleanup() {
        List<AgentMemoryEntry> archiveEntries = memoryService.findByTierAndUpdatedBefore(
            MemoryTier.ARCHIVE,
            Instant.now()
        );

        logService.log(
            LogLevel.INFO,
            LogCategory.MEMORY,
            Map.of("component", "AgentMemoryLifecycleService"),
            "MEMORY_ARCHIVE_CLEANUP_SCANNED",
            Map.of(
                "scannedArchiveEntries", archiveEntries.size(),
                "note", "Deduplication/merge policy scaffolding active; deep similarity merge to follow in later versions."
            ),
            null,
            null
        );
    }
}
