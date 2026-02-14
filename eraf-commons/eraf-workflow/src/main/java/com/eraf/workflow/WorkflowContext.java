package com.eraf.workflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Workflow Step 실행 시 전달되는 컨텍스트
 * <p>
 * Step 간 데이터 공유 및 워크플로우 메타데이터 접근을 위해 사용합니다.
 * 핸들러에서 변수를 읽고 쓸 수 있으며, 이 변수는 워크플로우 인스턴스에 자동으로 반영됩니다.
 */
public class WorkflowContext {

    private final String workflowInstanceId;
    private final String stepName;
    private final Map<String, Object> variables;

    public WorkflowContext(String workflowInstanceId, String stepName, Map<String, Object> variables) {
        this.workflowInstanceId = workflowInstanceId;
        this.stepName = stepName;
        this.variables = variables != null ? variables : new HashMap<>();
    }

    /**
     * Workflow 인스턴스 ID
     */
    public String getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    /**
     * 현재 실행 중인 Step 이름
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * 변수 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key) {
        return (T) variables.get(key);
    }

    /**
     * 변수 조회 (기본값 포함)
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue) {
        Object value = variables.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 변수 설정
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 변수 존재 여부
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    /**
     * 변수 제거
     */
    public void removeVariable(String key) {
        variables.remove(key);
    }

    /**
     * 전체 변수 맵 조회 (읽기 전용)
     */
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    /**
     * String 타입 변수 조회 (편의 메서드)
     */
    public String getString(String key) {
        Object value = variables.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Integer 타입 변수 조회 (편의 메서드)
     */
    public Integer getInteger(String key) {
        Object value = variables.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Boolean 타입 변수 조회 (편의 메서드)
     */
    public Boolean getBoolean(String key) {
        Object value = variables.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "WorkflowContext{instanceId='" + workflowInstanceId +
                "', step='" + stepName +
                "', variables=" + variables.keySet() + "}";
    }
}
