package dev.synapse.core.infrastructure.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.common.domain.SecurityAuditEvent;
import dev.synapse.core.common.domain.User;
import dev.synapse.core.common.repository.ConversationRepository;
import dev.synapse.core.common.repository.MessageRepository;
import dev.synapse.core.common.repository.SecurityAuditEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DataExportService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SecurityAuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public DataExportService(
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        SecurityAuditEventRepository auditEventRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Transactional(readOnly = true)
    public UserDataExport exportUserData(UUID userId, String username, String email,
                                          String role, Instant createdAt) {
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        List<ConversationExport> conversationExports = conversations.stream()
            .map(conv -> {
                long messageCount = messageRepository
                    .findByConversationIdOrderByCreatedAtAsc(conv.getId())
                    .size();
                return new ConversationExport(
                    conv.getId(),
                    conv.getAgentId(),
                    conv.getStartedAt(),
                    (int) messageCount
                );
            })
            .toList();

        List<SecurityAuditEvent> auditEvents = auditEventRepository
            .findByUserIdOrderByTimestampDesc(userId, PageRequest.of(0, Integer.MAX_VALUE));

        List<LoginHistoryEntry> loginHistory = auditEvents.stream()
            .map(event -> new LoginHistoryEntry(
                event.getId(),
                event.getEventType(),
                event.getAction(),
                event.getResource(),
                event.getResult(),
                event.getDetails(),
                event.getIpAddress(),
                event.getTimestamp()
            ))
            .toList();

        return new UserDataExport(
            new UserProfile(username, email, role, createdAt),
            conversationExports,
            loginHistory
        );
    }

    @Transactional(readOnly = true)
    public String exportUserDataAsJson(UUID userId, String username, String email,
                                        String role, Instant createdAt) {
        UserDataExport exportData = exportUserData(userId, username, email, role, createdAt);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(exportData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user data export", e);
        }
    }

    // --- Inner records ---

    public record UserDataExport(
        UserProfile profile,
        List<ConversationExport> conversations,
        List<LoginHistoryEntry> loginHistory
    ) {}

    public record UserProfile(
        String username,
        String email,
        String role,
        Instant createdAt
    ) {}

    public record ConversationExport(
        UUID id,
        String title,
        Instant createdAt,
        int messageCount
    ) {}

    public record LoginHistoryEntry(
        UUID id,
        String eventType,
        String action,
        String resource,
        String result,
        String details,
        String ipAddress,
        Instant timestamp
    ) {}
}
