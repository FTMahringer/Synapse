# RELEASE NOTES - v2.3.0

**Release Date:** 2026-05-10  
**Milestone:** Performance & Caching

---

## Overview

v2.3.0 finalizes the `v2.2.x` performance track with query optimization, Redis-backed caching, response compression, and connection pooling across database, cache, and provider HTTP integrations.

---

## Full v2.2.x Changelog

### v2.2.0 - Observability & Monitoring
- Released observability milestone
- Included: metrics infrastructure, distributed tracing, health checks, structured logging

### v2.2.1-dev - Query Optimization
- Added pagination to high-volume API endpoints
- Added composite performance indexes (`V12__performance_query_indexes.sql`)
- Removed costly provider scan from message send path
- Reduced bundle install lookup overhead using repository existence checks

### v2.2.2-dev - Redis Caching Layer
- Enabled Spring Cache with Redis backend
- Cached conversation history reads
- Cached model provider config/default provider lookups
- Cached user session-related lookups (id/username)
- Cached plugin and store metadata with mutation-triggered eviction

### v2.2.3-dev - Response Compression
- Enabled HTTP response compression (gzip)
- Added response size threshold and MIME filter
- Optimized DTO serialization footprint (`NON_NULL`)
- Added Jackson Blackbird module for serialization performance

### v2.2.4-dev - Connection Pooling
- Tuned PostgreSQL/Hikari connection pool settings
- Tuned Redis/Lettuce pool settings and timeouts
- Introduced shared pooled outbound HTTP client for provider services
- Added configurable HTTP client pool thread limits and connect timeout

---

## Validation Highlights

- Migration validation workflow passing with new index migration
- Compose smoke test hotfixed for version-agnostic Flyway success detection
- CodeQL passing on the latest performance branch state

---

## Upgrade Notes

No breaking API changes in v2.3.0.  
Operational teams should review environment defaults for:
- `SPRING_DATASOURCE_HIKARI_*`
- `SPRING_DATA_REDIS_LETTUCE_POOL_*`
- `SYNAPSE_HTTP_*`

---

## Tags Included

- `v2.2.0`
- `v2.2.1-dev`
- `v2.2.2-dev`
- `v2.2.3-dev`
- `v2.2.4-dev`
- `v2.3.0`
