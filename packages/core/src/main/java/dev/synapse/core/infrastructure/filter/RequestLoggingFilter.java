package dev.synapse.core.infrastructure.filter;

import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final SystemLogService logService;

    public RequestLoggingFilter(SystemLogService logService) {
        this.logService = logService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        if (shouldNotLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(request, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean shouldNotLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || 
               path.equals("/api/health") ||
               path.equals("/favicon.ico");
    }

    private void logRequest(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        int status = response.getStatus();
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        
        Map<String, Object> source = new HashMap<>();
        source.put("component", "RequestLoggingFilter");
        source.put("method", method);
        source.put("path", path);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("duration_ms", duration);
        payload.put("query", request.getQueryString());
        
        LogLevel level = status >= 500 ? LogLevel.ERROR : 
                        status >= 400 ? LogLevel.WARN : 
                        LogLevel.INFO;
        
        UUID correlationUuid = correlationId != null ? 
            parseUuidOrNull(correlationId) : null;
        
        logService.log(
            level,
            LogCategory.HTTP,
            source,
            "HTTP_REQUEST",
            payload,
            correlationUuid,
            null
        );
    }

    private UUID parseUuidOrNull(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
