package dev.synapse.core.conversation;

import dev.synapse.core.domain.Conversation;
import dev.synapse.core.domain.Message;
import dev.synapse.core.domain.ModelProvider;
import dev.synapse.core.exception.ResourceNotFoundException;
import dev.synapse.core.provider.ollama.OllamaChat;
import dev.synapse.core.provider.ollama.OllamaProviderService;
import dev.synapse.core.repository.ConversationRepository;
import dev.synapse.core.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ModelProviderService providerService;
    private final OllamaProviderService ollamaProviderService;
    private final MainAgentPromptService promptService;

    public MessageService(
        MessageRepository messageRepository,
        ConversationRepository conversationRepository,
        ModelProviderService providerService,
        OllamaProviderService ollamaProviderService,
        MainAgentPromptService promptService
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.providerService = providerService;
        this.ollamaProviderService = ollamaProviderService;
        this.promptService = promptService;
    }

    @Transactional
    public Message sendMessage(UUID conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation", conversationId.toString()));

        // Save user message
        Message userMessage = new Message();
        userMessage.setConversationId(conversationId);
        userMessage.setRole(Message.MessageRole.USER);
        userMessage.setContent(content);
        userMessage = messageRepository.save(userMessage);

        // Get conversation history
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // Get default provider (first enabled Ollama provider for now)
        ModelProvider provider = providerService.findAll().stream()
            .filter(p -> p.getType() == ModelProvider.ProviderType.OLLAMA && p.getEnabled())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No enabled Ollama provider found"));

        // Assemble prompt with Main Agent identity
        String systemPrompt = promptService.assemblePrompt();

        // Build chat messages
        List<OllamaChat.Message> chatMessages = new ArrayList<>();
        chatMessages.add(new OllamaChat.Message("system", systemPrompt));
        
        for (Message msg : history) {
            String role = msg.getRole() == Message.MessageRole.USER ? "user" : "assistant";
            chatMessages.add(new OllamaChat.Message(role, msg.getContent()));
        }

        // Get model from provider config
        String model = provider.getConfig() != null && provider.getConfig().containsKey("model")
            ? provider.getConfig().get("model").toString()
            : "llama3.2";

        // Call Ollama with timeout handling
        long startTime = System.currentTimeMillis();
        OllamaChat.ChatRequest request = new OllamaChat.ChatRequest(
            model,
            chatMessages,
            false,
            null
        );

        OllamaChat.ChatResponse response;
        try {
            response = ollamaProviderService.chatCompletion(provider, request);
        } catch (Exception e) {
            // Log error and create error message
            Message errorMessage = new Message();
            errorMessage.setConversationId(conversationId);
            errorMessage.setRole(Message.MessageRole.ASSISTANT);
            errorMessage.setContent("⚠️ Model provider error: " + e.getMessage());
            errorMessage.setProviderId(provider.getId());
            errorMessage.setModelName(model);
            errorMessage.setLatencyMs(System.currentTimeMillis() - startTime);
            messageRepository.save(errorMessage);
            
            throw new RuntimeException("Failed to get model response", e);
        }
        
        long latency = System.currentTimeMillis() - startTime;

        // Save assistant message with metadata
        Message assistantMessage = new Message();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole(Message.MessageRole.ASSISTANT);
        assistantMessage.setContent(response.message().content());
        assistantMessage.setProviderId(provider.getId());
        assistantMessage.setModelName(model);
        assistantMessage.setLatencyMs(latency);
        assistantMessage.setPromptTokens(response.promptEvalCount() != null ? response.promptEvalCount() : 0);
        assistantMessage.setCompletionTokens(response.evalCount() != null ? response.evalCount() : 0);
        assistantMessage.setTokens(
            assistantMessage.getPromptTokens() + assistantMessage.getCompletionTokens()
        );
        messageRepository.save(assistantMessage);

        return userMessage;
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
