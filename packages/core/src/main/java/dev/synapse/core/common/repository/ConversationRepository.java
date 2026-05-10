package dev.synapse.core.common.repository;

import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.common.domain.Conversation.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserId(UUID userId);
    List<Conversation> findByAgentId(String agentId);
    List<Conversation> findByUserIdAndStatus(UUID userId, ConversationStatus status);
}
