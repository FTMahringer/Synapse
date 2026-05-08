package dev.synapse.core.service;

import dev.synapse.core.domain.FirmProject;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.logging.LogCategory;
import dev.synapse.core.logging.LogLevel;
import dev.synapse.core.logging.SystemLogService;
import dev.synapse.core.repository.FirmProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entry point for dispatching projects to the AI-Firm.
 * Only the main-agent may dispatch projects. The firm is a singleton.
 */
@Service
public class AiFirmDispatchService {

    private static final String FIRM_ID = "my-firm";
    private static final String MAIN_AGENT_ID = "main-agent";

    private final FirmProjectRepository firmProjectRepository;
    private final SystemLogService logService;

    public AiFirmDispatchService(
        FirmProjectRepository firmProjectRepository,
        SystemLogService logService
    ) {
        this.firmProjectRepository = firmProjectRepository;
        this.logService = logService;
    }

    /**
     * Dispatch a project to the AI-Firm.
     * Validates the dispatcher is the main-agent (singleton firm contract).
     */
    @Transactional
    public FirmProject dispatchProject(
        String title,
        String description,
        String dispatchedByAgentId,
        UUID conversationId,
        UUID taskId
    ) {
        if (!MAIN_AGENT_ID.equals(dispatchedByAgentId)) {
            throw new IllegalArgumentException(
                "Only main-agent may dispatch projects to the AI-Firm. Received: " + dispatchedByAgentId
            );
        }

        FirmProject project = new FirmProject();
        project.setFirmId(FIRM_ID);
        project.setTitle(title);
        project.setDescription(description);
        project.setDispatchedByAgentId(dispatchedByAgentId);
        project.setConversationId(conversationId);
        project.setTaskId(taskId);
        project.setStatus(FirmProject.FirmProjectStatus.ACCEPTED);

        FirmProject saved = firmProjectRepository.save(project);

        logService.log(
            LogLevel.INFO,
            LogCategory.AI_FIRM,
            Map.of("component", "AiFirmDispatchService", "firmId", FIRM_ID),
            "firm.project.accepted",
            Map.of(
                "projectId", saved.getId().toString(),
                "title", title,
                "dispatchedBy", dispatchedByAgentId
            ),
            conversationId,
            null
        );

        return saved;
    }

    /**
     * Assign a project to a team and move it to IN_PROGRESS.
     */
    @Transactional
    public FirmProject assignToTeam(UUID projectId, String teamId) {
        FirmProject project = findById(projectId);
        project.setAssignedTeamId(teamId);
        project.setStatus(FirmProject.FirmProjectStatus.IN_PROGRESS);

        FirmProject saved = firmProjectRepository.save(project);

        logService.log(
            LogLevel.INFO,
            LogCategory.AI_FIRM,
            Map.of("component", "AiFirmDispatchService", "firmId", FIRM_ID),
            "firm.project.delegated",
            Map.of("projectId", projectId.toString(), "teamId", teamId),
            null,
            null
        );

        return saved;
    }

    /**
     * Mark a project as completed.
     */
    @Transactional
    public FirmProject completeProject(UUID projectId) {
        FirmProject project = findById(projectId);
        project.setStatus(FirmProject.FirmProjectStatus.COMPLETED);

        FirmProject saved = firmProjectRepository.save(project);

        logService.log(
            LogLevel.INFO,
            LogCategory.AI_FIRM,
            Map.of("component", "AiFirmDispatchService", "firmId", FIRM_ID),
            "firm.project.completed",
            Map.of("projectId", projectId.toString()),
            null,
            null
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public FirmProject findById(UUID projectId) {
        return firmProjectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("FirmProject", projectId.toString()));
    }

    @Transactional(readOnly = true)
    public List<FirmProject> findAll() {
        return firmProjectRepository.findByFirmIdOrderByCreatedAtDesc(FIRM_ID);
    }
}
