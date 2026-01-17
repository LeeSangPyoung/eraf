# ERAF Gateway Feature - Load Balancer

Kong Upstream-style Load Balancing with Health Checks for ERAF API Gateway.

## Overview

This module provides enterprise-grade load balancing capabilities for the ERAF API Gateway, inspired by Kong's upstream architecture. It distributes incoming traffic across multiple backend servers using various algorithms, with built-in health checks and support for advanced deployment strategies.

## Features

- **Multiple Load Balancing Algorithms**
  - Round Robin
  - Weighted Round Robin
  - Least Connections
  - Random
  - IP Hash (sticky sessions)

- **Active Health Checks**
  - Periodic HTTP health checks
  - Configurable intervals and timeouts
  - Automatic server marking (healthy/unhealthy)
  - Threshold-based status changes

- **Passive Health Checks**
  - Monitor actual traffic patterns
  - Track failure rates in real-time
  - Automatic circuit breaking per server

- **Advanced Deployment Strategies**
  - Canary deployment with percentage-based routing
  - Blue-green deployment support
  - Server versioning and tagging

- **Connection Management**
  - Connection pooling
  - Active connection tracking
  - Request statistics per server

## Load Balancing Algorithms

### 1. Round Robin (ROUND_ROBIN)

Distributes requests evenly across all healthy servers in sequential order.

**Best for:**
- Stateless applications
- Servers with similar capacity
- Simple, predictable traffic distribution

**Example:**
```yaml
eraf:
  gateway:
    load-balancer:
      default-algorithm: ROUND_ROBIN
```

**How it works:**
```
Request 1 → Server A
Request 2 → Server B
Request 3 → Server C
Request 4 → Server A (cycle repeats)
```

### 2. Weighted Round Robin (WEIGHTED_ROUND_ROBIN)

Distributes requests based on server weights. Higher weight = more requests.

**Best for:**
- Heterogeneous server configurations
- Gradual scaling scenarios
- Servers with different capacities

**Example:**
```yaml
upstream:
  name: api-backend
  algorithm: WEIGHTED_ROUND_ROBIN
  servers:
    - host: server-1
      port: 8080
      weight: 3  # Gets 3x more traffic
    - host: server-2
      port: 8080
      weight: 1  # Gets 1x traffic
```

**How it works:**
```
Request 1 → Server A (weight: 3)
Request 2 → Server A
Request 3 → Server A
Request 4 → Server B (weight: 1)
Request 5 → Server A (cycle repeats)
```

### 3. Least Connections (LEAST_CONNECTIONS)

Routes requests to the server with fewest active connections.

**Best for:**
- Long-lived connections
- Varying request processing times
- WebSocket or streaming applications
- Ensuring even load distribution

**Example:**
```yaml
eraf:
  gateway:
    load-balancer:
      default-algorithm: LEAST_CONNECTIONS
```

**How it works:**
```
Server A: 5 active connections
Server B: 3 active connections ← Next request goes here
Server C: 7 active connections
```

### 4. Random (RANDOM)

Randomly selects a server from the list of healthy servers.

**Best for:**
- Simple load distribution
- Stateless applications
- When predictability is not required
- Testing and development

**Example:**
```yaml
eraf:
  gateway:
    load-balancer:
      default-algorithm: RANDOM
```

### 5. IP Hash (IP_HASH)

Routes requests based on client IP address hash. Provides sticky sessions.

**Best for:**
- Stateful applications
- Session affinity requirements
- Consistent routing for same client
- Applications with server-side caching

**Example:**
```yaml
upstream:
  name: api-backend
  algorithm: IP_HASH
  sticky-session: true
```

**How it works:**
```
Client 192.168.1.10 → Always routes to Server A
Client 192.168.1.20 → Always routes to Server B
Client 192.168.1.10 → Always routes to Server A (consistent)
```

## Algorithm Comparison

| Algorithm | Use Case | Pros | Cons |
|-----------|----------|------|------|
| **Round Robin** | General purpose | Simple, predictable | Doesn't consider server load |
| **Weighted RR** | Mixed capacities | Flexible, capacity-aware | More complex configuration |
| **Least Connections** | Long connections | Load-aware, fair distribution | Slight overhead |
| **Random** | Simple distribution | Very simple | Less predictable |
| **IP Hash** | Sticky sessions | Session persistence | Uneven distribution possible |

## Configuration

### Basic Configuration

```yaml
eraf:
  gateway:
    load-balancer:
      enabled: true
      default-algorithm: ROUND_ROBIN
      connect-timeout: 5000
      read-timeout: 30000
```

### Health Check Configuration

```yaml
eraf:
  gateway:
    load-balancer:
      health-check:
        # Active health checks
        enabled: true
        interval: 10s
        timeout: 5s
        path: /health
        expected-statuses: [200, 204]
        healthy-threshold: 2      # 2 successes to mark healthy
        unhealthy-threshold: 3    # 3 failures to mark unhealthy

        # Passive health checks
        passive-enabled: true
        passive-unhealthy-threshold: 5
        passive-window: 1m
```

### Upstream Configuration

```java
Upstream upstream = Upstream.builder()
    .name("api-backend")
    .algorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN)
    .servers(Arrays.asList(
        Server.builder()
            .host("localhost")
            .port(8081)
            .weight(3)
            .build(),
        Server.builder()
            .host("localhost")
            .port(8082)
            .weight(1)
            .build()
    ))
    .healthCheck(HealthCheckConfig.builder()
        .enabled(true)
        .interval(Duration.ofSeconds(10))
        .build())
    .build();

loadBalancerService.saveUpstream(upstream);
```

## Health Checks

### Active Health Checks

Periodically sends HTTP requests to server health endpoints.

**Configuration:**
```yaml
health-check:
  enabled: true
  interval: 10s              # Check every 10 seconds
  timeout: 5s                # Timeout after 5 seconds
  path: /health              # Health endpoint path
  expected-statuses: [200]   # Expected HTTP status
  healthy-threshold: 2       # 2 consecutive successes
  unhealthy-threshold: 3     # 3 consecutive failures
```

**Health Check Flow:**
```
1. Send GET request to http://server:port/health
2. Wait for response (max 5 seconds)
3. Check if status code is 200
4. Track consecutive successes/failures
5. Update server health status if threshold reached
```

### Passive Health Checks

Monitors actual traffic and marks servers unhealthy based on real request failures.

**Configuration:**
```yaml
health-check:
  passive-enabled: true
  passive-unhealthy-threshold: 5    # 5 failures in window
  passive-window: 1m                # Within 1 minute
```

**Passive Check Flow:**
```
1. Monitor all proxied requests
2. Track failures per server in time window
3. If failures exceed threshold → mark server unhealthy
4. Active health checks will restore health status
```

### Health Status Transitions

```
Initial State: HEALTHY

HEALTHY --[3 consecutive failures]--> UNHEALTHY
UNHEALTHY --[2 consecutive successes]--> HEALTHY
```

## Canary Deployment

Deploy new versions gradually by routing a percentage of traffic to canary servers.

### Setup

```java
Upstream upstream = Upstream.builder()
    .name("api-backend")
    .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
    .canaryPercentage(10)           // 10% to canary
    .canaryVersion("v2")            // Canary version
    .servers(Arrays.asList(
        // Stable version (90% traffic)
        Server.builder()
            .host("server-v1-1")
            .port(8080)
            .version(null)          // Stable version
            .build(),
        Server.builder()
            .host("server-v1-2")
            .port(8080)
            .version(null)
            .build(),
        // Canary version (10% traffic)
        Server.builder()
            .host("server-v2")
            .port(8080)
            .version("v2")          // Canary version
            .build()
    ))
    .build();
```

### Gradual Rollout

```java
// Start with 5% canary traffic
upstream.setCanaryPercentage(5);

// Monitor metrics, gradually increase
upstream.setCanaryPercentage(10);
upstream.setCanaryPercentage(25);
upstream.setCanaryPercentage(50);

// Complete rollout
upstream.setCanaryPercentage(100);

// Or rollback if issues detected
upstream.setCanaryPercentage(0);
```

### Traffic Distribution

```
100 requests with 10% canary:
- 90 requests → Stable servers (v1)
- 10 requests → Canary server (v2)
```

## Blue-Green Deployment

Deploy new version to separate servers, then switch traffic instantly.

### Setup

```java
// Blue environment (current production)
Upstream blueUpstream = Upstream.builder()
    .name("api-blue")
    .servers(Arrays.asList(
        Server.builder().host("blue-1").port(8080).build(),
        Server.builder().host("blue-2").port(8080).build()
    ))
    .build();

// Green environment (new version)
Upstream greenUpstream = Upstream.builder()
    .name("api-green")
    .servers(Arrays.asList(
        Server.builder().host("green-1").port(8080).build(),
        Server.builder().host("green-2").port(8080).build()
    ))
    .build();

// Switch traffic by changing route upstream
route.setAttribute("upstream", "api-green");  // Switch to green
```

### Blue-Green Flow

```
1. Deploy new version to green environment
2. Run health checks on green servers
3. Verify green environment is healthy
4. Switch traffic from blue to green
5. Monitor green environment
6. Keep blue for rollback if needed
```

## Sticky Sessions

Use IP Hash algorithm for session affinity.

### Configuration

```java
Upstream upstream = Upstream.builder()
    .name("session-backend")
    .algorithm(LoadBalancerAlgorithm.IP_HASH)
    .stickySession(true)
    .servers(servers)
    .build();
```

### How It Works

```
Client IP: 192.168.1.10
Hash: 1234567890
Server Index: hash % server_count = 1
→ Always routes to Server B

Client IP: 192.168.1.20
Hash: 9876543210
Server Index: hash % server_count = 0
→ Always routes to Server A
```

### Limitations

- Server list changes affect routing
- Doesn't work behind NAT with single IP
- Use application-level sessions for true persistence

## Usage Examples

### Example 1: Simple Round Robin

```java
// Create upstream with 3 servers
Upstream upstream = Upstream.builder()
    .name("api-backend")
    .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
    .servers(Arrays.asList(
        Server.builder().host("server1").port(8080).build(),
        Server.builder().host("server2").port(8080).build(),
        Server.builder().host("server3").port(8080).build()
    ))
    .build();

loadBalancerService.saveUpstream(upstream);
```

### Example 2: Weighted Load Balancing

```java
// High-capacity server gets 3x traffic
Upstream upstream = Upstream.builder()
    .name("api-backend")
    .algorithm(LoadBalancerAlgorithm.WEIGHTED_ROUND_ROBIN)
    .servers(Arrays.asList(
        Server.builder()
            .host("high-capacity-server")
            .port(8080)
            .weight(3)
            .build(),
        Server.builder()
            .host("standard-server-1")
            .port(8080)
            .weight(1)
            .build(),
        Server.builder()
            .host("standard-server-2")
            .port(8080)
            .weight(1)
            .build()
    ))
    .build();

loadBalancerService.saveUpstream(upstream);
```

### Example 3: Least Connections for Long-Lived Connections

```java
// WebSocket or streaming backend
Upstream upstream = Upstream.builder()
    .name("websocket-backend")
    .algorithm(LoadBalancerAlgorithm.LEAST_CONNECTIONS)
    .servers(servers)
    .connectTimeout(10000)
    .readTimeout(300000)  // 5 minutes for long connections
    .build();

loadBalancerService.saveUpstream(upstream);
```

### Example 4: IP Hash for Sticky Sessions

```java
// Session-based application
Upstream upstream = Upstream.builder()
    .name("session-backend")
    .algorithm(LoadBalancerAlgorithm.IP_HASH)
    .stickySession(true)
    .servers(servers)
    .build();

loadBalancerService.saveUpstream(upstream);
```

### Example 5: Canary Deployment

```java
// 10% canary deployment
Upstream upstream = Upstream.builder()
    .name("api-backend")
    .algorithm(LoadBalancerAlgorithm.ROUND_ROBIN)
    .canaryPercentage(10)
    .canaryVersion("v2.0")
    .servers(Arrays.asList(
        // Production servers
        Server.builder()
            .host("prod-1")
            .port(8080)
            .version("v1.0")
            .build(),
        Server.builder()
            .host("prod-2")
            .port(8080)
            .version("v1.0")
            .build(),
        // Canary server
        Server.builder()
            .host("canary-1")
            .port(8080)
            .version("v2.0")
            .build()
    ))
    .build();

loadBalancerService.saveUpstream(upstream);
```

## Monitoring and Statistics

### Get Upstream Statistics

```java
UpstreamStats stats = loadBalancerService.getUpstreamStats("api-backend");

System.out.println("Total Servers: " + stats.getTotalServers());
System.out.println("Healthy Servers: " + stats.getHealthyServers());
System.out.println("Active Connections: " + stats.getTotalActiveConnections());
System.out.println("Total Requests: " + stats.getTotalRequests());
System.out.println("Failed Requests: " + stats.getTotalFailedRequests());
System.out.println("Success Rate: " + stats.getSuccessRatePercentage() + "%");
```

### Server Metrics

```java
Server server = upstream.getServers().get(0);

System.out.println("Host: " + server.getHost());
System.out.println("Healthy: " + server.isHealthy());
System.out.println("Active Connections: " + server.getActiveConnections().get());
System.out.println("Total Requests: " + server.getTotalRequests().get());
System.out.println("Failed Requests: " + server.getFailedRequests().get());
System.out.println("Failure Rate: " + server.getFailureRate());
```

## Best Practices

### 1. Choose the Right Algorithm

- **Round Robin**: Default choice for most applications
- **Weighted Round Robin**: When servers have different capacities
- **Least Connections**: For long-lived connections
- **IP Hash**: When session affinity is required

### 2. Configure Health Checks Properly

```yaml
# Production-ready health check configuration
health-check:
  enabled: true
  interval: 10s           # Not too frequent
  timeout: 5s             # Less than interval
  healthy-threshold: 2    # Cautious marking as healthy
  unhealthy-threshold: 3  # Quick failure detection
  passive-enabled: true   # Enable both active and passive
```

### 3. Monitor Server Health

```java
// Regularly check upstream health
UpstreamStats stats = loadBalancerService.getUpstreamStats("api-backend");
if (stats.getHealthyServers() < stats.getTotalServers() / 2) {
    // Alert: Less than 50% servers healthy
}
```

### 4. Use Canary Deployment for Safe Rollouts

```java
// Start small
upstream.setCanaryPercentage(5);
// Monitor for issues
// Gradually increase
upstream.setCanaryPercentage(10);
upstream.setCanaryPercentage(25);
// Complete rollout or rollback
```

### 5. Plan for Failures

```java
// Always configure multiple servers
// Enable health checks
// Set appropriate timeouts
// Monitor failure rates
```

## Integration with Circuit Breaker

The load balancer integrates with the circuit breaker module for enhanced resilience.

```java
// Circuit breaker per server
// When server circuit opens, health checker marks it unhealthy
// Load balancer automatically excludes unhealthy servers
// When server recovers, health checks restore it
```

## Troubleshooting

### No Available Servers

**Problem**: All servers marked as unhealthy.

**Solution**:
1. Check server health endpoints
2. Verify health check configuration
3. Review server logs for errors
4. Temporarily increase unhealthy threshold

### Uneven Load Distribution

**Problem**: Some servers getting more traffic than others.

**Solution**:
1. Verify algorithm choice (use LEAST_CONNECTIONS)
2. Check server weights (WEIGHTED_ROUND_ROBIN)
3. Ensure all servers are healthy
4. Monitor active connections per server

### Sticky Sessions Not Working

**Problem**: Same client routed to different servers.

**Solution**:
1. Use IP_HASH algorithm
2. Enable sticky-session flag
3. Check X-Forwarded-For headers
4. Verify client IP consistency

### High Failure Rate

**Problem**: Many failed requests.

**Solution**:
1. Enable passive health checks
2. Lower unhealthy threshold
3. Increase health check frequency
4. Review server capacity
5. Check network connectivity

## Dependencies

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-load-balancer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## License

Copyright (c) 2025 ERAF Project
