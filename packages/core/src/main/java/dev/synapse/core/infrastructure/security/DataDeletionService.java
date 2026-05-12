package dev.synapse.core.infrastructure.security;

import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.common.domain.SecurityAuditEvent;
import dev.synapse.core.common.domain.User;
import dev.synapse.core.common.repository.ConversationRepository;
import dev.synapse.core.common.repository.MessageRepository;
import dev.synapse.core.common.repository.SecurityAuditEventRepository;
import dev.synapse.core.common.repository.UserRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.users.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DataDeletionService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SecurityAuditEventRepository auditEventRepository;
    private final SecurityAuditService auditService;
    private final UserService userService;

    public DataDeletionService(
        UserRepository userRepository,
        ConversationRepository conversationRepository,
        MessageRepository messageRepository,
        SecurityAuditEventRepository auditEventRepository,
        SecurityAuditService auditService,
        UserService userService
    ) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditService = auditService;
        this.userService = userService;
    }

    @Transactional
    public void anonymizeUserData(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String anonymizedUsername = "deleted-user-" + userId;
        String anonymizedEmail = "deleted-" + userId + "@anon.local";

        user.setUsername(anonymizedUsername);
        user.setEmail(anonymizedEmail);
        userRepository.save(user);

        auditService.logUserAction(
            userId,
            anonymizedUsername,
            "ANONYMIZE",
            "/api/compliance/anonymize/" + userId,
            "User data anonymized for GDPR right-to-erasure",
            null
        );
    }

    @Transactional
    public void deleteUserData(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String username = user.getUsername();

        // Delete all conversations belonging to the user
        List<Conversation> conversations = conversationRepository.findByUserId(userId);
        for (Conversation conversation : conversations) {
            // Delete messages in each conversation
            messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                .forEach(messageRepository::delete);
            conversationRepository.deleteById(conversation.getId());
        }

        // Delete audit log entries referencing the user
        List<SecurityAuditEvent> auditEvents = auditEventRepository
            .findByUserIdOrderByTimestampDesc(userId,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
        auditEventRepository.deleteAll(auditEvents);

        // Log the deletion before removing the user
        auditService.logUserAction(
            userId,
            username,
            "DELETE",
            "/api/compliance/delete/" + userId,
            "All user data permanently deleted for GDPR right-to-erasure",
            null
        );

        // Finally, delete the user
        userService.deleteById(userId);
    }
}
