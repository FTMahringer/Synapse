# Security Deployment Checklist

## Pre-Deployment
- [ ] All default secrets overridden (`SYNAPSE_JWT_SECRET`, `SYNAPSE_ENCRYPTION_KEY`, `SPRING_DATASOURCE_PASSWORD`)
- [ ] Rate limiting configured for production load
- [ ] CORS restricted to known frontend origins
- [ ] Audit logging enabled and tested
- [ ] ADMIN users reviewed and validated
- [ ] Token expiration times configured appropriately
- [ ] Database backup strategy in place
- [ ] SSL/TLS certificates valid

## Deployment
- [ ] Docker Compose build succeeds
- [ ] Database migrations run without errors
- [ ] Backend health endpoint returns 200
- [ ] Authentication works (test login)
- [ ] Audit events are being recorded
- [ ] Rate limiting active (test with rapid requests)
- [ ] Security headers present (verify with curl)
- [ ] CORS headers correct (test from frontend origin)

## Post-Deployment
- [ ] Monitor logs for 30 minutes
- [ ] Check for unexpected errors
- [ ] Verify all team members can authenticate
- [ ] Run security validation tests
- [ ] Document any configuration changes
- [ ] Update runbook if new procedures discovered