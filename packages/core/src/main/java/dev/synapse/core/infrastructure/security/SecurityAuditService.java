package dev.synapse.core.infrastructure.security;

import dev.synapse.core.common.domain.SecurityAuditEvent;
import dev.synapse.core.common.repository.SecurityAuditEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SecurityAuditService {

    private final SecurityAuditEventRepository auditEventRepository;

    public SecurityAuditService(SecurityAuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void logEvent(String eventType, UUID userId, String username, String ipAddress,
                         String resource, String action, String result, String details) {
        SecurityAuditEvent event = new SecurityAuditEvent();
        event.setEventType(eventType);
        event.setUserId(userId);
        event.setUsername(username);
        event.setIpAddress(ipAddress);
        event.setResource(resource);
        event.setAction(action);
        event.setResult(result);
        event.setDetails(details);
        event.setTimestamp(Instant.now());
        auditEventRepository.save(event);
    }

    @Transactional
    public void logLoginAttempt(String username, String ipAddress, boolean success) {
        logEvent(
                success ? "LOGIN_SUCCESS" : "LOGIN_FAILED",
                null,
                username,
                ipAddress,
                "/api/auth/login",
                "LOGIN",
                success ? "SUCCESS" : "FAILURE",
                success ? "Successful login" : "Failed login attempt"
        );
    }

    @Transactional
    public void logAuthorizationDenied(UUID userId, String username, String resource, String ipAddress) {
        logEvent(
                "AUTHORIZATION_DENIED",
                userId,
                username,
                ipAddress,
                resource,
                "AUTHORIZATION",
                "DENIED",
                "Access denied to resource"
        );
    }

    @Transactional
    public void logUserAction(UUID userId, String username, String action, String resource,
                              String details, String ipAddress) {
        logEvent(
                "USER_ACTION",
                userId,
                username,
                ipAddress,
                resource,
                action,
                "SUCCESS",
                details
        );
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditEvent> queryEvents(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditEventRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditEvent> queryEventsByType(String eventType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditEventRepository.findByEventTypeOrderByTimestampDesc(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public List<SecurityAuditEvent> queryEventsByTimeRange(Instant from, Instant to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return auditEventRepository.findByTimestampBetween(from, to, pageable);
    }
}
