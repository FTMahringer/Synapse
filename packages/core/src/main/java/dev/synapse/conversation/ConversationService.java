package dev.synapse.conversation;

import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.common.repository.ConversationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Conversation create(String agentId, UUID userId) {
        Conversation conversation = new Conversation();
        conversation.setAgentId(agentId);
        conversation.setUserId(userId);
        conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    @Transactional(readOnly = true)
    public List<Conversation> findAll() {
        return conversationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Conversation> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        return conversationRepository.findAll(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public Conversation findById(UUID id) {
        return conversationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", id.toString()));
    }

    @Transactional(readOnly = true)
    public List<Conversation> findByUserId(UUID userId) {
        return conversationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Conversation> findByUserId(UUID userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        return conversationRepository.findByUserId(userId, pageRequest);
    }

    @Transactional
    public Conversation save(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!conversationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Conversation", id.toString());
        }
        conversationRepository.deleteById(id);
    }
}
