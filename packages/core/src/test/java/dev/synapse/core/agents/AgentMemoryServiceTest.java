package dev.synapse.core.agents;

import dev.synapse.agents.service.AgentMemoryService;
import dev.synapse.core.common.domain.AgentMemoryEntry;
import dev.synapse.core.common.domain.AgentMemoryEntry.MemoryTier;
import dev.synapse.core.common.domain.AgentMemoryEntry.PromotionReason;
import dev.synapse.core.common.repository.AgentMemoryRepository;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentMemoryServiceTest {

    @Mock
    private AgentMemoryRepository memoryRepository;

    @Mock
    private SystemLogService logService;

    private AgentMemoryService memoryService;

    @BeforeEach
    void setUp() {
        memoryService = new AgentMemoryService(memoryRepository, logService, 2);
    }

    @Test
    void read_promotesShortTermToKnowledgeAfterReuseThreshold() {
        AgentMemoryEntry entry = new AgentMemoryEntry();
        entry.setId(UUID.randomUUID());
        entry.setAgentId("main-agent");
        entry.setKey("project.summary");
        entry.setValue("summary");
        entry.setTier(MemoryTier.SHORT_TERM);
        entry.setAccessCount(1);

        when(memoryRepository.findByAgentIdAndKey("main-agent", "project.summary")).thenReturn(Optional.of(entry));
        when(memoryRepository.save(any(AgentMemoryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        memoryService.read("main-agent", "project.summary");

        assertEquals(MemoryTier.KNOWLEDGE, entry.getTier());
        assertEquals(PromotionReason.REUSED, entry.getPromotionReason());
        assertEquals(2, entry.getAccessCount());
        verify(memoryRepository, times(1)).save(entry);
    }

    @Test
    void promote_rejectsInvalidArchiveToShortTermTransition() {
        AgentMemoryEntry entry = new AgentMemoryEntry();
        entry.setId(UUID.randomUUID());
        entry.setAgentId("main-agent");
        entry.setKey("archived");
        entry.setTier(MemoryTier.ARCHIVE);

        when(memoryRepository.findByAgentIdAndKey("main-agent", "archived")).thenReturn(Optional.of(entry));

        assertThrows(
            ValidationException.class,
            () -> memoryService.promote("main-agent", "archived", MemoryTier.SHORT_TERM, PromotionReason.MANUAL)
        );
    }
}
