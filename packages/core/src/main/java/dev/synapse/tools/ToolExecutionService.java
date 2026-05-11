package dev.synapse.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.synapse.agents.service.AgentHardeningPolicyService;
import dev.synapse.agents.service.HardeningDecision;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ToolExecutionService {

    private static final String TOOL_CACHE_NAME = "native-tools";

    private final ToolRegistryService toolRegistryService;
    private final CacheManager cacheManager;
    private final ObjectMapper canonicalObjectMapper;
    private final SystemLogService logService;
    private final AgentHardeningPolicyService hardeningPolicyService;
    private final boolean cacheEnabled;
    private final long defaultCacheTtlSeconds;
    private final long executionTimeoutMs;
    private final ExecutorService executorService;

    public ToolExecutionService(
        ToolRegistryService toolRegistryService,
        CacheManager cacheManager,
        ObjectMapper objectMapper,
        SystemLogService logService,
        AgentHardeningPolicyService hardeningPolicyService,
        @Value("${synapse.tools.cache.enabled:true}") boolean cacheEnabled,
        @Value("${synapse.tools.cache.default-ttl-seconds:300}") long defaultCacheTtlSeconds,
        @Value("${synapse.tools.execution-timeout-ms:10000}") long executionTimeoutMs
    ) {
        this.toolRegistryService = toolRegistryService;
        this.cacheManager = cacheManager;
        this.logService = logService;
        this.hardeningPolicyService = hardeningPolicyService;
        this.cacheEnabled = cacheEnabled;
        this.defaultCacheTtlSeconds = defaultCacheTtlSeconds;
        this.executionTimeoutMs = executionTimeoutMs;
        this.canonicalObjectMapper = objectMapper.copy().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public ToolExecutionResponse execute(
        String toolId,
        ToolExecutionContext context,
        Map<String, Object> input
    ) {
        ToolExecutionContext effectiveContext = context != null
            ? context
            : new ToolExecutionContext(null, null, null, null);
        NativeJavaTool tool = toolRegistryService.getTool(toolId);
        Map<String, Object> effectiveInput = input != null ? input : Map.of();
        tool.validateInput(effectiveInput);
        long estimatedTokens = estimateTokens(effectiveInput);
        HardeningDecision hardeningDecision = hardeningPolicyService.evaluateTokenBudget(
            effectiveContext.agentId(),
            effectiveContext.teamId(),
            "TOOLING",
            estimatedTokens
        );
        if (hardeningDecision.decision() == HardeningDecision.Decision.BLOCK) {
            throw new ValidationException("Tool execution blocked by hardening policy: " + hardeningDecision.reasonCode());
        }

        String cacheKey = buildCacheKey(tool.toolId(), effectiveContext, effectiveInput);
        Cache cache = cacheEnabled && tool.isCacheable() ? cacheManager.getCache(TOOL_CACHE_NAME) : null;
        if (cache != null) {
            ToolExecutionResponse cachedResponse = resolveCached(cache, cacheKey, tool.toolId());
            if (cachedResponse != null) {
                if (hardeningDecision.decision() == HardeningDecision.Decision.WARN) {
                    return new ToolExecutionResponse(
                        cachedResponse.toolId(),
                        cachedResponse.status(),
                        cachedResponse.result(),
                        cachedResponse.cached(),
                        cachedResponse.cacheTtlRemainingSeconds(),
                        cachedResponse.executedAt(),
                        hardeningDecision.reasonCode(),
                        hardeningDecision.enforcedMode()
                    );
                }
                return cachedResponse;
            }
        }

        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "ToolExecutionService", "toolId", tool.toolId()),
            "NATIVE_TOOL_EXECUTION_STARTED",
            Map.of("cacheable", String.valueOf(tool.isCacheable())),
            null,
            null
        );

        ToolExecutionResult result = executeWithTimeout(tool, effectiveContext, effectiveInput);
        if (result == null) {
            throw new ToolExecutionException("Tool '" + tool.toolId() + "' returned no result");
        }
        Instant executedAt = Instant.now();
        ToolExecutionResponse response = new ToolExecutionResponse(
            tool.toolId(),
            "SUCCESS",
            result.result(),
            false,
            null,
            executedAt,
            hardeningDecision.reasonCode(),
            hardeningDecision.enforcedMode()
        );

        if (cache != null) {
            long ttlSeconds = tool.cacheTtlSeconds() > 0 ? tool.cacheTtlSeconds() : defaultCacheTtlSeconds;
            Instant expiresAt = executedAt.plusSeconds(ttlSeconds);
            cache.put(cacheKey, new ToolCacheEntry(result.result(), expiresAt));
        }

        logService.log(
            LogLevel.INFO,
            LogCategory.AGENT,
            Map.of("component", "ToolExecutionService", "toolId", tool.toolId()),
            "NATIVE_TOOL_EXECUTION_COMPLETED",
            Map.of("status", "SUCCESS"),
            null,
            null
        );

        return response;
    }

    private ToolExecutionResult executeWithTimeout(
        NativeJavaTool tool,
        ToolExecutionContext context,
        Map<String, Object> input
    ) {
        CompletableFuture<ToolExecutionResult> future = CompletableFuture.supplyAsync(
            () -> tool.execute(context, input),
            executorService
        );
        try {
            return future.get(executionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ToolExecutionTimeoutException(
                "Tool '" + tool.toolId() + "' timed out after " + executionTimeoutMs + "ms"
            );
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ToolExecutionException("Tool '" + tool.toolId() + "' failed: " + cause.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolExecutionException("Tool execution interrupted for '" + tool.toolId() + "'");
        }
    }

    private ToolExecutionResponse resolveCached(Cache cache, String cacheKey, String toolId) {
        ToolCacheEntry entry = cache.get(cacheKey, ToolCacheEntry.class);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAt().isBefore(Instant.now())) {
            cache.evict(cacheKey);
            return null;
        }
        long ttlRemaining = Duration.between(Instant.now(), entry.expiresAt()).toSeconds();
        return new ToolExecutionResponse(
            toolId,
            "SUCCESS",
            entry.result(),
            true,
            Math.max(ttlRemaining, 0),
            Instant.now(),
            "OK",
            null
        );
    }

    private long estimateTokens(Map<String, Object> input) {
        String canonical = canonicalize(input);
        return Math.max(1, Math.round(canonical.length() / 4.0));
    }

    private String buildCacheKey(String toolId, ToolExecutionContext context, Map<String, Object> input) {
        String canonicalInput = canonicalize(input);
        String scopeKey = "agent=" + nonNull(context.agentId())
            + "|team=" + nonNull(context.teamId())
            + "|session=" + nonNull(context.collaborationSessionId())
            + "|goal=" + nonNull(context.goalId());
        return hash(toolId + "|" + scopeKey + "|" + canonicalInput);
    }

    private String canonicalize(Map<String, Object> input) {
        try {
            return canonicalObjectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new ValidationException("Unable to normalize tool input for cache key");
        }
    }

    private String hash(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new ToolExecutionException("Failed to compute tool cache key hash");
        }
    }

    private String nonNull(Object value) {
        return value != null ? value.toString() : "";
    }

    @PreDestroy
    void shutdownExecutor() {
        executorService.shutdown();
    }
}
