package dev.synapse.core.service;

import dev.synapse.core.domain.Message;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.repository.ConversationRepository;
import dev.synapse.core.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageService(
        MessageRepository messageRepository,
        ConversationRepository conversationRepository
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Message sendMessage(UUID conversationId, String content) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ResourceNotFoundException("Conversation", conversationId.toString());
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(Message.MessageRole.USER);
        message.setContent(content);

        return messageRepository.save(message);
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
