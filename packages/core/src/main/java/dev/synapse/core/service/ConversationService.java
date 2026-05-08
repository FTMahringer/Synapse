package dev.synapse.core.service;

import dev.synapse.core.domain.Conversation;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.repository.ConversationRepository;
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

    @Transactional(readOnly = true)
    public List<Conversation> findAll() {
        return conversationRepository.findAll();
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
