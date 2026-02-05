package com.eraf.saga.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Saga 정의 메타데이터
 * 런타임에 등록된 Saga 정보를 보관합니다.
 */
public class SagaDefinition {

    private String name;
    private String description;
    private Class<?> sagaClass;
    private Object sagaInstance;
    private long timeout;
    private int maxRetries;
    private List<StepDefinition> steps;

    public SagaDefinition() {
        this.steps = new ArrayList<>();
    }

    public SagaDefinition(String name, Class<?> sagaClass) {
        this();
        this.name = name;
        this.sagaClass = sagaClass;
    }

    public void addStep(StepDefinition step) {
        this.steps.add(step);
        // 순서대로 정렬
        this.steps.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
    }

    public StepDefinition getStep(int order) {
        return steps.stream()
                .filter(s -> s.getOrder() == order)
                .findFirst()
                .orElse(null);
    }

    public StepDefinition getStepByName(String name) {
        return steps.stream()
                .filter(s -> name.equals(s.getName()))
                .findFirst()
                .orElse(null);
    }

    public int getTotalSteps() {
        return steps.size();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class<?> getSagaClass() {
        return sagaClass;
    }

    public void setSagaClass(Class<?> sagaClass) {
        this.sagaClass = sagaClass;
    }

    public Object getSagaInstance() {
        return sagaInstance;
    }

    public void setSagaInstance(Object sagaInstance) {
        this.sagaInstance = sagaInstance;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    public void setSteps(List<StepDefinition> steps) {
        this.steps = steps;
    }

    /**
     * Step 정의
     */
    public static class StepDefinition {

        private int order;
        private String name;
        private Method method;
        private Method compensateMethod;
        private long timeout;
        private int retries;
        private long retryDelay;

        public StepDefinition() {}

        public StepDefinition(int order, String name, Method method) {
            this.order = order;
            this.name = name;
            this.method = method;
        }

        // Getters and Setters
        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Method getCompensateMethod() {
            return compensateMethod;
        }

        public void setCompensateMethod(Method compensateMethod) {
            this.compensateMethod = compensateMethod;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public int getRetries() {
            return retries;
        }

        public void setRetries(int retries) {
            this.retries = retries;
        }

        public long getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
        }

        public boolean hasCompensation() {
            return compensateMethod != null;
        }
    }
}
