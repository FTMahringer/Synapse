# SYNAPSE Metrics & Monitoring

SYNAPSE exposes Prometheus metrics at `/actuator/prometheus`. This allows integration with existing monitoring infrastructure **or** use of the bundled Prometheus + Grafana stack.

## Option 1: Use Existing Prometheus (Recommended for Homelab/Enterprise)

If you already have Prometheus running in your environment, add SYNAPSE as a scrape target:

```yaml
# Add to your existing prometheus.yml
scrape_configs:
  - job_name: 'synapse-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['<synapse-host>:8080']
        labels:
          application: 'synapse-core'
          environment: 'production'
```

Reload Prometheus configuration:

```bash
curl -X POST http://localhost:9090/-/reload
```

## Option 2: Deploy Bundled Prometheus + Grafana

For standalone deployments, use the provided monitoring stack:

```bash
cd installer/compose
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml --profile monitoring up -d
```

This deploys:
- **Prometheus** on `http://localhost:9090` - metrics storage and querying
- **Grafana** on `http://localhost:3001` - visualization dashboards (admin/admin)

### Access Prometheus

Navigate to `http://localhost:9090` and query SYNAPSE metrics:

```promql
# JVM memory usage
jvm_memory_used_bytes{application="synapse-core"}

# HTTP request rate
rate(http_server_requests_seconds_count[5m])

# Agent invocation count
synapse_agent_invocations_total
```

### Access Grafana

1. Navigate to `http://localhost:3001`
2. Login: `admin` / `admin` (change on first login)
3. Prometheus datasource is pre-configured
4. Import dashboards from `installer/compose/grafana/dashboards/`

## Available Metrics

SYNAPSE exposes these metric categories:

### System Metrics
- **JVM memory, GC, threads** - `jvm_*`
- **HTTP requests** - `http_server_requests_*`
- **Database connections** - `hikaricp_*`
- **Redis connections** - `lettuce_*`

### Application Metrics
- **Agent invocations** - `synapse_agent_invocations_total`
- **Conversation messages** - `synapse_messages_total`
- **Model provider calls** - `synapse_provider_requests_total`
- **Plugin operations** - `synapse_plugin_*`

### Custom Metrics

All metrics are tagged with:
- `application=synapse-core`
- `version=<synapse-version>`
- `service=synapse-core`

## Production Considerations

### External Prometheus

For production environments:

1. **Use service discovery** instead of static targets (Kubernetes, Consul, etc.)
2. **Configure remote storage** for long-term metrics retention
3. **Set up alerting rules** for critical metrics
4. **Enable authentication** on Prometheus and Grafana

### Metrics Retention

Default bundled Prometheus retention: **15 days**

To change:

```yaml
# docker-compose.monitoring.yml
prometheus:
  command:
    - '--storage.tsdb.retention.time=30d'  # 30 days retention
```

### Performance Impact

Metrics scraping has minimal overhead:
- Endpoint response time: <10ms
- Memory footprint: ~5MB
- CPU impact: <1% during scrape

## Example Queries

### Request Rate by Endpoint
```promql
rate(http_server_requests_seconds_count{uri!="/actuator/prometheus"}[5m])
```

### 95th Percentile Response Time
```promql
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))
```

### Active Agent Sessions
```promql
synapse_agent_active_sessions
```

### Error Rate
```promql
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

## Troubleshooting

### Metrics endpoint returns 404

Ensure `spring-boot-starter-actuator` and `micrometer-registry-prometheus` dependencies are present.

### No metrics scraped

1. Check Prometheus targets: `http://localhost:9090/targets`
2. Verify backend is reachable from Prometheus container
3. Check logs: `docker compose logs prometheus`

### Grafana shows "no data"

1. Verify Prometheus datasource configuration
2. Check time range in dashboard (default: last 6 hours)
3. Ensure metrics are being scraped: check Prometheus UI

## Next Steps

- **v2.1.2-dev**: Distributed tracing with trace IDs
- **v2.1.3-dev**: Enhanced health checks
- **v2.1.4-dev**: Structured JSON logging
