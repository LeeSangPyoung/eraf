package com.eraf.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF Observability Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.observability")
public class ErafObservabilityProperties {

    /**
     * Observability 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 서비스 이름
     */
    private String serviceName = "eraf-service";

    /**
     * 서비스 버전
     */
    private String serviceVersion = "1.0.0";

    /**
     * 환경 (dev, staging, prod)
     */
    private String environment = "dev";

    /**
     * Tracing 설정
     */
    private Tracing tracing = new Tracing();

    /**
     * Metrics 설정
     */
    private Metrics metrics = new Metrics();

    /**
     * Logging 설정
     */
    private Logging logging = new Logging();

    /**
     * Exporter 설정
     */
    private Exporter exporter = new Exporter();

    public static class Tracing {
        /**
         * Tracing 활성화 여부
         */
        private boolean enabled = true;

        /**
         * 샘플링 비율 (0.0 ~ 1.0)
         * 0.1 = 10%, 1.0 = 100%
         */
        private double samplingRate = 1.0;

        /**
         * HTTP 요청 자동 계측
         */
        private boolean autoInstrumentHttp = true;

        /**
         * DB 쿼리 자동 계측
         */
        private boolean autoInstrumentDb = true;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public double getSamplingRate() { return samplingRate; }
        public void setSamplingRate(double samplingRate) { this.samplingRate = samplingRate; }
        public boolean isAutoInstrumentHttp() { return autoInstrumentHttp; }
        public void setAutoInstrumentHttp(boolean autoInstrumentHttp) { this.autoInstrumentHttp = autoInstrumentHttp; }
        public boolean isAutoInstrumentDb() { return autoInstrumentDb; }
        public void setAutoInstrumentDb(boolean autoInstrumentDb) { this.autoInstrumentDb = autoInstrumentDb; }
    }

    public static class Metrics {
        /**
         * Metrics 활성화 여부
         */
        private boolean enabled = true;

        /**
         * JVM 메트릭 수집
         */
        private boolean includeJvm = true;

        /**
         * HTTP 메트릭 수집
         */
        private boolean includeHttp = true;

        /**
         * 커스텀 메트릭 활성화
         */
        private boolean enableCustom = true;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isIncludeJvm() { return includeJvm; }
        public void setIncludeJvm(boolean includeJvm) { this.includeJvm = includeJvm; }
        public boolean isIncludeHttp() { return includeHttp; }
        public void setIncludeHttp(boolean includeHttp) { this.includeHttp = includeHttp; }
        public boolean isEnableCustom() { return enableCustom; }
        public void setEnableCustom(boolean enableCustom) { this.enableCustom = enableCustom; }
    }

    public static class Logging {
        /**
         * Log correlation 활성화 (trace_id, span_id 자동 추가)
         */
        private boolean enableCorrelation = true;

        /**
         * JSON 로그 포맷 사용
         */
        private boolean useJsonFormat = false;

        // Getters and Setters
        public boolean isEnableCorrelation() { return enableCorrelation; }
        public void setEnableCorrelation(boolean enableCorrelation) { this.enableCorrelation = enableCorrelation; }
        public boolean isUseJsonFormat() { return useJsonFormat; }
        public void setUseJsonFormat(boolean useJsonFormat) { this.useJsonFormat = useJsonFormat; }
    }

    public static class Exporter {
        /**
         * Exporter 타입: otlp, jaeger, zipkin, logging
         */
        private String type = "otlp";

        /**
         * OTLP Endpoint
         */
        private String endpoint = "http://localhost:4317";

        /**
         * gRPC 사용 여부 (false면 HTTP)
         */
        private boolean useGrpc = true;

        /**
         * 인증 헤더 (예: Bearer token)
         */
        private String authHeader;

        /**
         * Timeout (milliseconds)
         */
        private long timeoutMs = 10000;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public boolean isUseGrpc() { return useGrpc; }
        public void setUseGrpc(boolean useGrpc) { this.useGrpc = useGrpc; }
        public String getAuthHeader() { return authHeader; }
        public void setAuthHeader(String authHeader) { this.authHeader = authHeader; }
        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long timeoutMs) { this.timeoutMs = timeoutMs; }
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceVersion() { return serviceVersion; }
    public void setServiceVersion(String serviceVersion) { this.serviceVersion = serviceVersion; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public Tracing getTracing() { return tracing; }
    public void setTracing(Tracing tracing) { this.tracing = tracing; }
    public Metrics getMetrics() { return metrics; }
    public void setMetrics(Metrics metrics) { this.metrics = metrics; }
    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }
    public Exporter getExporter() { return exporter; }
    public void setExporter(Exporter exporter) { this.exporter = exporter; }
}
