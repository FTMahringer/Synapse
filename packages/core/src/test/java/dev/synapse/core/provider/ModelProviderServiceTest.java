package dev.synapse.providers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.common.repository.ModelProviderRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.infrastructure.security.SecretEncryptionService;
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
class ModelProviderServiceTest {

    @Mock
    private ModelProviderRepository modelProviderRepository;

    @Mock
    private SecretEncryptionService encryptionService;

    @Mock
    private SystemLogService logService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ModelProviderService modelProviderService;

    private ModelProvider testProvider;

    @BeforeEach
    void setUp() {
        testProvider = new ModelProvider();
        testProvider.setId(UUID.randomUUID());
        testProvider.setName("ollama");
        testProvider.setType(ModelProvider.ProviderType.OLLAMA);
        testProvider.setEnabled(true);
    }

    @Test
    void findAll_shouldReturnAllProviders() {
        ModelProvider provider2 = new ModelProvider();
        provider2.setId(UUID.randomUUID());
        provider2.setName("openai");
        provider2.setType(ModelProvider.ProviderType.OPENAI);
        List<ModelProvider> providers = Arrays.asList(testProvider, provider2);

        when(modelProviderRepository.findAll()).thenReturn(providers);

        List<ModelProvider> result = modelProviderService.findAll();

        assertEquals(2, result.size());
        verify(modelProviderRepository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnProviderWhenExists() {
        UUID providerId = testProvider.getId();

        when(modelProviderRepository.findById(providerId)).thenReturn(
            Optional.of(testProvider)
        );

        ModelProvider result = modelProviderService.findById(providerId);

        assertNotNull(result);
        assertEquals(providerId, result.getId());
        assertEquals("ollama", result.getName());
        verify(modelProviderRepository, times(1)).findById(providerId);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        UUID providerId = UUID.randomUUID();

        when(modelProviderRepository.findById(providerId)).thenReturn(
            Optional.empty()
        );

        assertThrows(ResourceNotFoundException.class, () ->
            modelProviderService.findById(providerId)
        );
        verify(modelProviderRepository, times(1)).findById(providerId);
    }

    @Test
    void findByName_shouldReturnProviderWhenExists() {
        String providerName = "ollama";

        when(modelProviderRepository.findByName(providerName)).thenReturn(
            Optional.of(testProvider)
        );

        ModelProvider result = modelProviderService.findByName(providerName);

        assertNotNull(result);
        assertEquals(providerName, result.getName());
        verify(modelProviderRepository, times(1)).findByName(providerName);
    }

    @Test
    void findByName_shouldThrowExceptionWhenNotFound() {
        String providerName = "nonexistent";

        when(modelProviderRepository.findByName(providerName)).thenReturn(
            Optional.empty()
        );

        assertThrows(ResourceNotFoundException.class, () ->
            modelProviderService.findByName(providerName)
        );
        verify(modelProviderRepository, times(1)).findByName(providerName);
    }

    @Test
    void deleteById_shouldDeleteWhenExists() {
        UUID providerId = testProvider.getId();

        when(modelProviderRepository.existsById(providerId)).thenReturn(true);
        doNothing().when(modelProviderRepository).deleteById(providerId);
        doNothing()
            .when(logService)
            .log(any(), any(), any(), any(), any(), any(), any());

        modelProviderService.deleteById(providerId);

        verify(modelProviderRepository, times(1)).existsById(providerId);
        verify(modelProviderRepository, times(1)).deleteById(providerId);
        verify(logService, times(1)).log(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void deleteById_shouldThrowExceptionWhenNotFound() {
        UUID providerId = UUID.randomUUID();

        when(modelProviderRepository.existsById(providerId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
            modelProviderService.deleteById(providerId)
        );
        verify(modelProviderRepository, times(1)).existsById(providerId);
        verify(modelProviderRepository, never()).deleteById(any());
    }

    @Test
    void findEnabled_shouldReturnOnlyEnabledProviders() {
        ModelProvider disabledProvider = new ModelProvider();
        disabledProvider.setId(UUID.randomUUID());
        disabledProvider.setName("disabled");
        disabledProvider.setEnabled(false);

        List<ModelProvider> enabledProviders = List.of(testProvider);

        when(modelProviderRepository.findByEnabledTrue()).thenReturn(
            enabledProviders
        );

        List<ModelProvider> result = modelProviderService.findEnabled();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnabled());
        verify(modelProviderRepository, times(1)).findByEnabledTrue();
    }
}
