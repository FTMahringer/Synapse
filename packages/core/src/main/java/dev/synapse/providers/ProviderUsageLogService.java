package dev.synapse.providers;

import dev.synapse.core.common.domain.ModelProvider;
import dev.synapse.core.common.domain.ProviderUsageLog;
import dev.synapse.core.common.repository.ProviderUsageLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ProviderUsageLogService {

    private final ProviderUsageLogRepository repository;

    public ProviderUsageLogService(ProviderUsageLogRepository repository) {
        this.repository = repository;
    }

    public void logUsage(
        ModelProvider provider,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        Long latencyMs,
        boolean success,
        String errorMessage
    ) {
        ProviderUsageLog log = new ProviderUsageLog();
        log.setProviderId(provider.getId());
        log.setProviderName(provider.getName());
        log.setProviderType(provider.getType().name());
        log.setModel(model);
        log.setPromptTokens(promptTokens);
        log.setCompletionTokens(completionTokens);
        log.setTotalTokens(
            (promptTokens != null ? promptTokens : 0) + 
            (completionTokens != null ? completionTokens : 0)
        );
        log.setLatencyMs(latencyMs);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        
        repository.save(log);
    }

    public List<ProviderUsageLog> getProviderHistory(UUID providerId) {
        return repository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    public List<ProviderUsageLog> getProviderHistorySince(UUID providerId, LocalDateTime since) {
        return repository.findByProviderIdAndCreatedAtAfterOrderByCreatedAtDesc(providerId, since);
    }

    public Map<String, Object> getProviderStats(UUID providerId) {
        Double avgLatency = repository.averageLatencyForProvider(providerId);
        Long totalTokens = repository.totalTokensForProvider(providerId);
        Long failureCount = repository.failureCountForProvider(providerId);
        Long totalCalls = repository.count();
        
        return Map.of(
            "averageLatencyMs", avgLatency != null ? avgLatency : 0.0,
            "totalTokens", totalTokens != null ? totalTokens : 0L,
            "failureCount", failureCount != null ? failureCount : 0L,
            "totalCalls", totalCalls,
            "successRate", totalCalls > 0 
                ? (totalCalls - (failureCount != null ? failureCount : 0L)) / (double) totalCalls 
                : 1.0
        );
    }
}
