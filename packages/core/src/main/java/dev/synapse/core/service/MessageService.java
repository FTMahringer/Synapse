package dev.synapse.core.service;

import dev.synapse.core.domain.Message;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<Message> findByConversationId(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional(readOnly = true)
    public Message findById(UUID id) {
        return messageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Message", id.toString()));
    }

    @Transactional
    public Message save(Message message) {
        return messageRepository.save(message);
    }
}
