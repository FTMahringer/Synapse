package dev.synapse.core.conversation;

import dev.synapse.core.domain.Conversation;
import dev.synapse.core.domain.Message;
import dev.synapse.core.dto.CreateConversationRequest;
import dev.synapse.core.repository.MessageRepository;
import dev.synapse.core.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageRepository messageRepository;

    public ConversationController(
        ConversationService conversationService,
        MessageRepository messageRepository
    ) {
        this.conversationService = conversationService;
        this.messageRepository = messageRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Conversation create(
        @Valid @RequestBody CreateConversationRequest request,
        @RequestHeader(value = "X-User-ID", required = false) UUID userId
    ) {
        if (userId == null) {
            userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
        return conversationService.create(request.agentId(), userId);
    }

    @GetMapping
    public List<Conversation> list(
        @RequestHeader(value = "X-User-ID", required = false) UUID userId
    ) {
        if (userId == null) {
            return conversationService.findAll();
        }
        return conversationService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public Conversation get(@PathVariable UUID id) {
        return conversationService.findById(id);
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable UUID id) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(id);
    }
}
