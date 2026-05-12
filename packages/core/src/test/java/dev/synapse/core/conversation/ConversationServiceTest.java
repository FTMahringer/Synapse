package dev.synapse.conversation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.synapse.core.common.domain.Conversation;
import dev.synapse.core.common.repository.ConversationRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationService conversationService;

    private UUID testUserId;
    private String testAgentId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAgentId = "main-agent";
    }

    @Test
    void create_shouldSaveAndReturnConversation() {
        Conversation savedConversation = new Conversation();
        savedConversation.setId(UUID.randomUUID());
        savedConversation.setAgentId(testAgentId);
        savedConversation.setUserId(testUserId);
        savedConversation.setStatus(Conversation.ConversationStatus.ACTIVE);

        when(conversationRepository.save(any(Conversation.class))).thenReturn(
            savedConversation
        );

        Conversation result = conversationService.create(
            testAgentId,
            testUserId
        );

        assertNotNull(result);
        assertEquals(testAgentId, result.getAgentId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(
            Conversation.ConversationStatus.ACTIVE,
            result.getStatus()
        );
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void findAll_shouldReturnAllConversations() {
        Conversation conv1 = new Conversation();
        conv1.setId(UUID.randomUUID());
        Conversation conv2 = new Conversation();
        conv2.setId(UUID.randomUUID());
        List<Conversation> conversations = Arrays.asList(conv1, conv2);

        when(conversationRepository.findAll()).thenReturn(conversations);

        List<Conversation> result = conversationService.findAll();

        assertEquals(2, result.size());
        verify(conversationRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnConversationWhenExists() {
        UUID conversationId = UUID.randomUUID();
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);

        when(conversationRepository.findById(conversationId)).thenReturn(
            Optional.of(conversation)
        );

        Conversation result = conversationService.findById(conversationId);

        assertNotNull(result);
        assertEquals(conversationId, result.getId());
        verify(conversationRepository, times(1)).findById(conversationId);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.findById(conversationId)).thenReturn(
            Optional.empty()
        );

        assertThrows(ResourceNotFoundException.class, () ->
            conversationService.findById(conversationId)
        );
        verify(conversationRepository, times(1)).findById(conversationId);
    }

    @Test
    void findByUserId_shouldReturnUserConversations() {
        Conversation conv1 = new Conversation();
        conv1.setId(UUID.randomUUID());
        conv1.setUserId(testUserId);
        Conversation conv2 = new Conversation();
        conv2.setId(UUID.randomUUID());
        conv2.setUserId(testUserId);
        List<Conversation> conversations = Arrays.asList(conv1, conv2);

        when(conversationRepository.findByUserId(testUserId)).thenReturn(
            conversations
        );

        List<Conversation> result = conversationService.findByUserId(
            testUserId
        );

        assertEquals(2, result.size());
        result.forEach(conv -> assertEquals(testUserId, conv.getUserId()));
        verify(conversationRepository, times(1)).findByUserId(testUserId);
    }

    @Test
    void save_shouldSaveAndReturnConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());

        when(conversationRepository.save(conversation)).thenReturn(
            conversation
        );

        Conversation result = conversationService.save(conversation);

        assertNotNull(result);
        assertEquals(conversation.getId(), result.getId());
        verify(conversationRepository, times(1)).save(conversation);
    }

    @Test
    void deleteById_shouldDeleteWhenExists() {
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.existsById(conversationId)).thenReturn(
            true
        );
        doNothing().when(conversationRepository).deleteById(conversationId);

        conversationService.deleteById(conversationId);

        verify(conversationRepository, times(1)).existsById(conversationId);
        verify(conversationRepository, times(1)).deleteById(conversationId);
    }

    @Test
    void deleteById_shouldThrowExceptionWhenNotFound() {
        UUID conversationId = UUID.randomUUID();

        when(conversationRepository.existsById(conversationId)).thenReturn(
            false
        );

        assertThrows(ResourceNotFoundException.class, () ->
            conversationService.deleteById(conversationId)
        );
        verify(conversationRepository, times(1)).existsById(conversationId);
        verify(conversationRepository, never()).deleteById(any());
    }
}
