package dev.synapse.core.infrastructure.security;

import dev.synapse.core.common.domain.SecurityAuditEvent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final SecurityAuditService auditService;

    public AuditLogController(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SecurityAuditEvent> queryEvents(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (userId != null) {
            return auditService.queryEvents(userId, page, size);
        }
        if (type != null && !type.isBlank()) {
            return auditService.queryEventsByType(type, page, size);
        }
        if (from != null && to != null) {
            return auditService.queryEventsByTimeRange(from, to, page, size);
        }

        // Default: return all events (fallback)
        return auditService.queryEventsByTimeRange(
                Instant.EPOCH,
                Instant.now(),
                page,
                size
        );
    }
}
