package com.eraf.sample.controller.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 모니터링/API 문서 컨트롤러
 * #64 Health Check, #65 Metrics 대시보드
 * #66 분산 추적 조회, #67 Swagger UI 연동
 */
@Controller
@RequestMapping("/monitoring")
public class MonitoringController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;

    @Autowired(required = false)
    private MetricsEndpoint metricsEndpoint;

    // In-memory simulation data
    private final Map<String, ComponentHealth> mockHealthData = new ConcurrentHashMap<>();
    private final Map<String, MetricData> mockMetrics = new ConcurrentHashMap<>();
    private final List<TraceRecord> traceHistory = new CopyOnWriteArrayList<>();
    private final AtomicLong requestCounter = new AtomicLong(0);
    private final long startTime = System.currentTimeMillis();

    @PostConstruct
    public void init() {
        initHealthData();
        initMetricsData();
        initSampleTraces();
    }

    private void initHealthData() {
        mockHealthData.put("application", new ComponentHealth("application", "UP", Map.of("version", "1.0.0", "name", "eraf-sample-biz")));
        mockHealthData.put("db", new ComponentHealth("db", "UP", Map.of("database", "H2", "validationQuery", "isValid()")));
        mockHealthData.put("redis", new ComponentHealth("redis", "UP", Map.of("cluster_size", "3", "version", "7.0.0")));
        mockHealthData.put("kafka", new ComponentHealth("kafka", "UP", Map.of("brokers", "localhost:9092", "topics", "10")));
        mockHealthData.put("diskSpace", new ComponentHealth("diskSpace", "UP", Map.of("total", "500GB", "free", "350GB", "threshold", "10GB")));
    }

    private void initMetricsData() {
        mockMetrics.put("jvm.memory.used", new MetricData("jvm.memory.used", 256.5, "MB", "JVM Used Memory"));
        mockMetrics.put("jvm.memory.max", new MetricData("jvm.memory.max", 512.0, "MB", "JVM Max Memory"));
        mockMetrics.put("jvm.threads.live", new MetricData("jvm.threads.live", 45.0, "", "Live Threads"));
        mockMetrics.put("system.cpu.usage", new MetricData("system.cpu.usage", 0.35, "%", "CPU Usage"));
        mockMetrics.put("http.server.requests.count", new MetricData("http.server.requests.count", 12345.0, "", "Total HTTP Requests"));
        mockMetrics.put("http.server.requests.duration.avg", new MetricData("http.server.requests.duration.avg", 45.5, "ms", "Avg Response Time"));
        mockMetrics.put("cache.hits", new MetricData("cache.hits", 8500.0, "", "Cache Hits"));
        mockMetrics.put("cache.misses", new MetricData("cache.misses", 1500.0, "", "Cache Misses"));
        mockMetrics.put("db.connections.active", new MetricData("db.connections.active", 5.0, "", "Active DB Connections"));
        mockMetrics.put("db.connections.max", new MetricData("db.connections.max", 20.0, "", "Max DB Connections"));
    }

    private void initSampleTraces() {
        // Sample traces
        addSampleTrace("GET /api/users", 45L, "SUCCESS");
        addSampleTrace("POST /api/orders", 150L, "SUCCESS");
        addSampleTrace("GET /api/products/123", 30L, "SUCCESS");
        addSampleTrace("POST /api/payments", 500L, "ERROR");
        addSampleTrace("GET /api/reports", 2500L, "SUCCESS");
    }

    private void addSampleTrace(String operation, long durationMs, String status) {
        TraceRecord trace = new TraceRecord();
        trace.setTraceId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        trace.setSpanId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        trace.setOperation(operation);
        trace.setDurationMs(durationMs);
        trace.setStatus(status);
        trace.setTimestamp(LocalDateTime.now().minusMinutes(new Random().nextInt(60)));
        trace.setService("eraf-sample-biz");

        List<SpanRecord> spans = new ArrayList<>();
        spans.add(createSpan("http.request", trace.getSpanId(), null, 0, (int) durationMs));
        if (operation.contains("orders") || operation.contains("payments")) {
            spans.add(createSpan("db.query", null, trace.getSpanId(), 5, 20));
            spans.add(createSpan("cache.get", null, trace.getSpanId(), 30, 5));
            if (operation.contains("payments")) {
                spans.add(createSpan("external.payment-gateway", null, trace.getSpanId(), 40, (int) (durationMs - 100)));
            }
        }
        trace.setSpans(spans);

        traceHistory.add(0, trace);
    }

    private SpanRecord createSpan(String name, String spanId, String parentId, int offsetMs, int durationMs) {
        SpanRecord span = new SpanRecord();
        span.setSpanId(spanId != null ? spanId : UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        span.setParentSpanId(parentId);
        span.setName(name);
        span.setDurationMs(durationMs);
        span.setOffsetMs(offsetMs);
        return span;
    }

    @GetMapping
    public String monitoringPage() {
        return "monitoring/monitoring";
    }

    // ============ #64 Health Check ============

    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> getHealth() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Try to get from actual HealthEndpoint
        if (healthEndpoint != null) {
            try {
                var health = healthEndpoint.health();
                result.put("status", health.getStatus().getCode());
                result.put("source", "actuator");
            } catch (Exception e) {
                log.warn("Failed to get health from actuator: {}", e.getMessage());
            }
        }

        // Use mock data for comprehensive view
        String overallStatus = mockHealthData.values().stream()
                .allMatch(h -> "UP".equals(h.getStatus())) ? "UP" : "DOWN";

        result.put("status", overallStatus);
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));
        result.put("components", mockHealthData);

        return result;
    }

    @GetMapping("/health/{component}")
    @ResponseBody
    public Map<String, Object> getComponentHealth(@PathVariable String component) {
        ComponentHealth health = mockHealthData.get(component);
        if (health == null) {
            return Map.of("error", "Component not found: " + component);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", component);
        result.put("status", health.getStatus());
        result.put("details", health.getDetails());

        return result;
    }

    @PostMapping("/health/{component}/toggle")
    @ResponseBody
    public Map<String, Object> toggleComponentHealth(@PathVariable String component) {
        ComponentHealth health = mockHealthData.get(component);
        if (health == null) {
            return Map.of("success", false, "error", "Component not found");
        }

        String newStatus = "UP".equals(health.getStatus()) ? "DOWN" : "UP";
        health.setStatus(newStatus);

        log.info("[Health] Toggled {} to {}", component, newStatus);

        return Map.of("success", true, "component", component, "status", newStatus);
    }

    // ============ #65 Metrics 대시보드 ============

    @GetMapping("/metrics")
    @ResponseBody
    public Map<String, Object> getMetrics() {
        // Update some metrics dynamically
        updateDynamicMetrics();

        Map<String, Object> result = new LinkedHashMap<>();

        // System metrics
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("uptime", formatUptime(System.currentTimeMillis() - startTime));
        system.put("uptimeMs", System.currentTimeMillis() - startTime);
        system.put("requestCount", requestCounter.get());

        result.put("system", system);
        result.put("metrics", mockMetrics.values());
        result.put("timestamp", LocalDateTime.now().format(FORMATTER));

        return result;
    }

    @GetMapping("/metrics/{name}")
    @ResponseBody
    public Map<String, Object> getMetric(@PathVariable String name) {
        MetricData metric = mockMetrics.get(name);
        if (metric == null) {
            return Map.of("error", "Metric not found: " + name);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", metric.getName());
        result.put("value", metric.getValue());
        result.put("unit", metric.getUnit());
        result.put("description", metric.getDescription());

        // Generate some historical data
        List<Map<String, Object>> history = new ArrayList<>();
        double baseValue = metric.getValue();
        for (int i = 10; i >= 0; i--) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("time", LocalDateTime.now().minusMinutes(i).format(FORMATTER));
            point.put("value", baseValue * (0.9 + Math.random() * 0.2));
            history.add(point);
        }
        result.put("history", history);

        return result;
    }

    @PostMapping("/metrics/increment")
    @ResponseBody
    public Map<String, Object> incrementRequest() {
        long count = requestCounter.incrementAndGet();
        mockMetrics.get("http.server.requests.count").setValue(count + 12345);

        return Map.of("success", true, "requestCount", count);
    }

    private void updateDynamicMetrics() {
        // Simulate dynamic values
        mockMetrics.get("jvm.memory.used").setValue(200 + Math.random() * 100);
        mockMetrics.get("system.cpu.usage").setValue(0.1 + Math.random() * 0.5);
        mockMetrics.get("jvm.threads.live").setValue(40 + Math.random() * 20);
        mockMetrics.get("db.connections.active").setValue(Math.floor(Math.random() * 10));
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    // ============ #66 분산 추적 조회 ============

    @GetMapping("/traces")
    @ResponseBody
    public Map<String, Object> getTraces(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "20") int limit) {

        List<TraceRecord> filtered = traceHistory.stream()
                .filter(t -> status == null || status.isEmpty() || status.equals(t.getStatus()))
                .limit(limit)
                .toList();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", (long) traceHistory.size());
        stats.put("success", traceHistory.stream().filter(t -> "SUCCESS".equals(t.getStatus())).count());
        stats.put("error", traceHistory.stream().filter(t -> "ERROR".equals(t.getStatus())).count());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", stats);
        result.put("traces", filtered);

        return result;
    }

    @GetMapping("/traces/{traceId}")
    @ResponseBody
    public Map<String, Object> getTrace(@PathVariable String traceId) {
        TraceRecord trace = traceHistory.stream()
                .filter(t -> traceId.equals(t.getTraceId()))
                .findFirst()
                .orElse(null);

        if (trace == null) {
            return Map.of("error", "Trace not found: " + traceId);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trace", trace);

        return result;
    }

    @PostMapping("/traces/generate")
    @ResponseBody
    public Map<String, Object> generateTrace(@RequestBody Map<String, Object> request) {
        String operation = (String) request.getOrDefault("operation", "GET /api/test");
        int durationMs = (int) request.getOrDefault("durationMs", 100);
        String status = (String) request.getOrDefault("status", "SUCCESS");

        addSampleTrace(operation, durationMs, status);

        TraceRecord latest = traceHistory.get(0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("traceId", latest.getTraceId());
        result.put("operation", operation);
        result.put("durationMs", durationMs);
        result.put("status", status);

        return result;
    }

    @DeleteMapping("/traces/clear")
    @ResponseBody
    public Map<String, Object> clearTraces() {
        int count = traceHistory.size();
        traceHistory.clear();

        return Map.of("success", true, "clearedTraces", count);
    }

    // ============ #67 Swagger UI 연동 ============

    @GetMapping("/swagger/info")
    @ResponseBody
    public Map<String, Object> getSwaggerInfo() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("swaggerUiUrl", "/swagger-ui.html");
        result.put("openApiUrl", "/v3/api-docs");
        result.put("info", Map.of(
                "title", "ERAF Sample API",
                "version", "1.0.0",
                "description", "ERAF Framework Sample Application API"
        ));

        // Sample API endpoints
        List<Map<String, Object>> endpoints = new ArrayList<>();
        endpoints.add(Map.of("method", "GET", "path", "/api/users", "description", "Get all users"));
        endpoints.add(Map.of("method", "POST", "path", "/api/users", "description", "Create user"));
        endpoints.add(Map.of("method", "GET", "path", "/api/orders", "description", "Get all orders"));
        endpoints.add(Map.of("method", "POST", "path", "/api/orders", "description", "Create order"));
        endpoints.add(Map.of("method", "GET", "path", "/api/products", "description", "Get all products"));

        result.put("endpoints", endpoints);

        return result;
    }

    // ============ DTOs ============

    public static class ComponentHealth {
        private String name;
        private String status;
        private Map<String, String> details;

        public ComponentHealth() {}

        public ComponentHealth(String name, String status, Map<String, String> details) {
            this.name = name;
            this.status = status;
            this.details = details;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Map<String, String> getDetails() { return details; }
        public void setDetails(Map<String, String> details) { this.details = details; }
    }

    public static class MetricData {
        private String name;
        private double value;
        private String unit;
        private String description;

        public MetricData() {}

        public MetricData(String name, double value, String unit, String description) {
            this.name = name;
            this.value = value;
            this.unit = unit;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class TraceRecord {
        private String traceId;
        private String spanId;
        private String operation;
        private long durationMs;
        private String status;
        private LocalDateTime timestamp;
        private String service;
        private List<SpanRecord> spans;

        public String getTraceId() { return traceId; }
        public void setTraceId(String traceId) { this.traceId = traceId; }
        public String getSpanId() { return spanId; }
        public void setSpanId(String spanId) { this.spanId = spanId; }
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public List<SpanRecord> getSpans() { return spans; }
        public void setSpans(List<SpanRecord> spans) { this.spans = spans; }
        public String getTimestampFormatted() { return timestamp != null ? timestamp.format(FORMATTER) : null; }
    }

    public static class SpanRecord {
        private String spanId;
        private String parentSpanId;
        private String name;
        private int durationMs;
        private int offsetMs;

        public String getSpanId() { return spanId; }
        public void setSpanId(String spanId) { this.spanId = spanId; }
        public String getParentSpanId() { return parentSpanId; }
        public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getDurationMs() { return durationMs; }
        public void setDurationMs(int durationMs) { this.durationMs = durationMs; }
        public int getOffsetMs() { return offsetMs; }
        public void setOffsetMs(int offsetMs) { this.offsetMs = offsetMs; }
    }
}
