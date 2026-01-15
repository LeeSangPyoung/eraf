package com.eraf.core.logging;

import com.eraf.core.context.ErafContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 구조화된 JSON 로거
 */
public class StructuredLogger {

    private final Logger logger;

    public StructuredLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public StructuredLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }

    /**
     * INFO 레벨 구조화 로그
     */
    public void info(String message, Object... keyValues) {
        if (logger.isInfoEnabled()) {
            logger.info(formatMessage(message, keyValues));
        }
    }

    /**
     * DEBUG 레벨 구조화 로그
     */
    public void debug(String message, Object... keyValues) {
        if (logger.isDebugEnabled()) {
            logger.debug(formatMessage(message, keyValues));
        }
    }

    /**
     * WARN 레벨 구조화 로그
     */
    public void warn(String message, Object... keyValues) {
        if (logger.isWarnEnabled()) {
            logger.warn(formatMessage(message, keyValues));
        }
    }

    /**
     * ERROR 레벨 구조화 로그
     */
    public void error(String message, Object... keyValues) {
        logger.error(formatMessage(message, keyValues));
    }

    /**
     * ERROR 레벨 구조화 로그 (예외 포함)
     */
    public void error(String message, Throwable throwable, Object... keyValues) {
        logger.error(formatMessage(message, keyValues), throwable);
    }

    /**
     * 빌더 방식 로깅
     */
    public LogBuilder builder() {
        return new LogBuilder(logger);
    }

    private String formatMessage(String message, Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message);
        sb.append(" {");

        for (int i = 0; i < keyValues.length - 1; i += 2) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(keyValues[i]).append("\": ");
            Object value = keyValues[i + 1];
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 로그 빌더
     */
    public static class LogBuilder {
        private final Logger logger;
        private String message;
        private final Map<String, Object> fields = new LinkedHashMap<>();
        private Throwable throwable;

        public LogBuilder(Logger logger) {
            this.logger = logger;
        }

        public LogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public LogBuilder field(String key, Object value) {
            fields.put(key, value);
            return this;
        }

        public LogBuilder exception(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public LogBuilder withContext() {
            fields.put("traceId", ErafContext.getTraceId());
            fields.put("userId", ErafContext.getCurrentUserId());
            fields.put("clientIp", ErafContext.getClientIp());
            return this;
        }

        public void info() {
            if (logger.isInfoEnabled()) {
                log(org.slf4j.event.Level.INFO);
            }
        }

        public void debug() {
            if (logger.isDebugEnabled()) {
                log(org.slf4j.event.Level.DEBUG);
            }
        }

        public void warn() {
            if (logger.isWarnEnabled()) {
                log(org.slf4j.event.Level.WARN);
            }
        }

        public void error() {
            log(org.slf4j.event.Level.ERROR);
        }

        private void log(org.slf4j.event.Level level) {
            String formatted = formatFields();
            switch (level) {
                case INFO -> logger.info(formatted);
                case DEBUG -> logger.debug(formatted);
                case WARN -> logger.warn(formatted);
                case ERROR -> {
                    if (throwable != null) {
                        logger.error(formatted, throwable);
                    } else {
                        logger.error(formatted);
                    }
                }
                default -> logger.info(formatted);
            }
        }

        private String formatFields() {
            StringBuilder sb = new StringBuilder();
            if (message != null) {
                sb.append(message).append(" ");
            }
            sb.append("{");

            boolean first = true;
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("\"").append(entry.getKey()).append("\": ");
                Object value = entry.getValue();
                if (value instanceof String) {
                    sb.append("\"").append(value).append("\"");
                } else {
                    sb.append(value);
                }
                first = false;
            }

            sb.append("}");
            return sb.toString();
        }
    }
}
