package dev.synapse.conversation;

import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.common.domain.Message;
import dev.synapse.core.dto.CreateConversationRequest;
import dev.synapse.core.dto.SendMessageRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    public ConversationController(
        ConversationService conversationService,
        MessageService messageService
    ) {
        this.conversationService = conversationService;
        this.messageService = messageService;
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
        return messageService.findByConversationId(id);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(
        @PathVariable UUID id,
        @Valid @RequestBody SendMessageRequest request
    ) {
        return messageService.sendMessage(id, request.content());
    }
}
