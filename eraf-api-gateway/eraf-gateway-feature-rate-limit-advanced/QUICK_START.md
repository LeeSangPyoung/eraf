# Quick Start Guide - Advanced Rate Limit

## 1. Add Dependency

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-rate-limit-advanced</artifactId>
</dependency>
```

## 2. Basic Configuration (Local Mode)

Create `application.yml`:

```yaml
eraf:
  gateway:
    rate-limit-advanced:
      enabled: true
      default-algorithm: TOKEN_BUCKET
      default-max-requests: 100
      default-window-seconds: 60
```

That's it! The module will auto-configure.

## 3. Test It

```bash
# Make 100 requests
for i in {1..100}; do
  curl http://localhost:8080/api/test
done

# 101st request should be rate limited (429)
curl -v http://localhost:8080/api/test
```

Response headers:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Algorithm: TOKEN_BUCKET
Retry-After: 45
```

## 4. Enable Redis (Distributed Mode)

### Start Redis
```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### Configure
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      enabled: true
      distributed-mode: true  # Enable Redis
      redis:
        host: localhost
        port: 6379
```

## 5. Algorithm Selection

### Token Bucket (Recommended)
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      default-algorithm: TOKEN_BUCKET
      default-burst-size: 150     # Allow bursts up to 150
      default-refill-rate: 10.0   # 10 tokens per second
```

### Leaky Bucket (Smooth Rate)
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      default-algorithm: LEAKY_BUCKET
      default-refill-rate: 5.0    # Process 5 requests per second
```

### Sliding Window (Strict)
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      default-algorithm: SLIDING_WINDOW
      default-max-requests: 100
      default-window-seconds: 60
```

### Fixed Window (Fast)
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      default-algorithm: FIXED_WINDOW
      default-max-requests: 100
      default-window-seconds: 60
```

## 6. Path-Based Limits

```yaml
eraf:
  gateway:
    rate-limit-advanced:
      path-limits:
        "/api/v1/upload/**": 10      # 10 requests per minute
        "/api/v1/search/**": 100      # 100 requests per minute
        "/api/v1/public/**": 1000     # 1000 requests per minute
```

## 7. Consumer-Based Limits

```yaml
eraf:
  gateway:
    rate-limit-advanced:
      consumers:
        premium-user:
          id: premium-user
          max-requests: 1000
          algorithm: TOKEN_BUCKET
        basic-user:
          id: basic-user
          max-requests: 100
          algorithm: FIXED_WINDOW
```

Client must send header:
```bash
curl -H "X-API-Key: premium-user" http://localhost:8080/api/test
```

## 8. Exclude Paths

```yaml
eraf:
  gateway:
    rate-limit-advanced:
      exclude-patterns:
        - /health
        - /actuator/**
        - /public/**
```

## 9. Monitoring

### Check Rate Limit Info
Response headers include:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 45
X-RateLimit-Algorithm: TOKEN_BUCKET
```

### Monitor Redis (if distributed)
```bash
redis-cli INFO stats
redis-cli KEYS "rate-limit:*"
redis-cli HGETALL "rate-limit:token:rule-id:IP:192.168.1.1"
```

## 10. Production Setup

```yaml
spring:
  profiles:
    active: production

eraf:
  gateway:
    rate-limit-advanced:
      enabled: true
      distributed-mode: true
      default-algorithm: TOKEN_BUCKET
      redis:
        cluster: true
        cluster-nodes:
          - redis-1:7000
          - redis-2:7001
          - redis-3:7002
        password: ${REDIS_PASSWORD}
```

## Common Use Cases

### API Gateway
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      default-algorithm: TOKEN_BUCKET
      default-max-requests: 1000
      default-burst-size: 1500
      path-limits:
        "/api/v1/public/**": 10000
        "/api/v1/internal/**": 100
```

### File Upload Service
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      path-algorithms:
        "/api/v1/upload/**": LEAKY_BUCKET
      path-limits:
        "/api/v1/upload/**": 10
      default-refill-rate: 0.5  # Slow and steady
```

### Search API
```yaml
eraf:
  gateway:
    rate-limit-advanced:
      path-algorithms:
        "/api/v1/search/**": SLIDING_WINDOW
      path-limits:
        "/api/v1/search/**": 100
```

## Troubleshooting

### Rate limit not working?
1. Check `enabled: true`
2. Verify path patterns
3. Check exclude patterns
4. Enable debug: `logging.level.com.eraf.gateway.ratelimit.advanced=DEBUG`

### Redis connection failed?
- Falls back to local mode automatically
- Check Redis host/port
- Verify network connectivity

### High memory usage?
- Use FIXED_WINDOW instead of SLIDING_WINDOW
- Reduce window size
- Enable Redis distributed mode

## Next Steps

- Read [README.md](README.md) for detailed documentation
- Review [MODULE_SUMMARY.md](MODULE_SUMMARY.md) for architecture
- Check [application-rate-limit-advanced.yml](src/main/resources/application-rate-limit-advanced.yml) for examples
