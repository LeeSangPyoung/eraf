package com.eraf.saga.core;

import com.eraf.saga.execution.SagaExecution;

import java.util.HashMap;
import java.util.Map;

/**
 * Saga 실행 컨텍스트
 * Step 간 데이터 공유를 위한 컨텍스트입니다.
 */
public class SagaContext {

    private static final ThreadLocal<SagaContext> CURRENT = new ThreadLocal<>();

    private final SagaExecution execution;
    private final Map<String, Object> data;
    private Object input;

    public SagaContext(SagaExecution execution) {
        this.execution = execution;
        this.data = new HashMap<>();
    }

    public SagaContext(SagaExecution execution, Object input) {
        this(execution);
        this.input = input;
    }

    public static SagaContext current() {
        return CURRENT.get();
    }

    public static void setCurrent(SagaContext context) {
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }

    /**
     * 데이터 저장
     */
    public void put(String key, Object value) {
        this.data.put(key, value);
        this.execution.putContext(key, value);
    }

    /**
     * 데이터 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) this.data.get(key);
    }

    /**
     * 데이터 조회 (기본값 포함)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object value = this.data.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 데이터 존재 여부
     */
    public boolean has(String key) {
        return this.data.containsKey(key);
    }

    /**
     * Saga 실행 ID
     */
    public String getSagaId() {
        return execution.getId();
    }

    /**
     * Saga 이름
     */
    public String getSagaName() {
        return execution.getSagaName();
    }

    /**
     * Trace ID
     */
    public String getTraceId() {
        return execution.getTraceId();
    }

    /**
     * 현재 Step 번호
     */
    public int getCurrentStep() {
        return execution.getCurrentStep();
    }

    /**
     * 입력 데이터
     */
    @SuppressWarnings("unchecked")
    public <T> T getInput() {
        return (T) input;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public SagaExecution getExecution() {
        return execution;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
}
