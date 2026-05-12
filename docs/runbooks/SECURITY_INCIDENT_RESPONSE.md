# Security Incident Response Runbook

## Severity Levels
- **CRITICAL**: Active breach, data exfiltration, service compromise
- **HIGH**: Authentication bypass, privilege escalation, secret exposure
- **MEDIUM**: Rate limit bypass, audit log gaps, misconfiguration
- **LOW**: Best practice violations, minor information disclosure

## Response Procedures

### CRITICAL: Suspected Breach
1. Isolate the affected service (scale to 0, block network)
2. Rotate ALL secrets (JWT, encryption keys, DB passwords)
3. Revoke ALL tokens via `synapse auth revoke-all`
4. Audit `security_audit_events` for unauthorized access
5. Restore from last known good backup
6. Notify all users to rotate credentials

### HIGH: Secret Exposure
1. Rotate exposed secret immediately
2. Update `.env` with new secret
3. Restart affected services
4. Audit logs for unauthorized access using old secret
5. Update incident timeline in runbook

### MEDIUM: Rate Limit Bypass
1. Review rate limiting configuration
2. Check `RateLimitingFilter` logs for abuse patterns
3. Adjust `rate-limiting.requests-per-minute` if needed
4. Block offending IPs if necessary

### MEDIUM: Audit Log Gap
1. Identify the gap period
2. Check `SecurityAuditService` for errors
3. Verify `security_audit_events` table is receiving events
4. Ensure `JwtAuthenticationFilter` is logging denials

## Recovery Procedures

### Service Restoration
1. Verify Docker Compose stack is healthy
2. Check `synapse health` endpoint
3. Verify authentication works with test login
4. Confirm audit events are being recorded
5. Monitor logs for 15 minutes

### Data Restoration
1. Restore PostgreSQL from backup
2. Run Flyway migrations if needed
3. Verify data integrity
4. Re-encrypt secrets if encryption key was rotated

## Communication Templates

### User Notification
```
Subject: Security Incident Notification - [DATE]
We have detected and addressed a security incident affecting SYNAPSE.
- Impact: [DESCRIPTION]
- Actions taken: [ACTIONS]
- Recommended action: Please rotate your password
- Contact: security@synapse.dev
```

### Post-Incident Report
```
# Post-Incident Report
## Summary
- Date: 
- Severity: 
- Duration: 
- Root Cause: 
## Timeline
- [TIME] Detection
- [TIME] Containment
- [TIME] Eradication
- [TIME] Recovery
## Lessons Learned
- 
## Action Items
- [ ]
```

## Prevention Checklist
- [ ] Default secrets are overridden in production
- [ ] Rate limiting is configured
- [ ] Audit logging is enabled
- [ ] CORS is restricted to known origins
- [ ] Security headers are present
- [ ] ADMIN role is properly assigned
- [ ] Token revocation works
- [ ] GDPR compliance endpoints are accessible