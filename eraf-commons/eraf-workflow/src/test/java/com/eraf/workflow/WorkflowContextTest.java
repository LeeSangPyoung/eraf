package com.eraf.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowContext 테스트")
class WorkflowContextTest {

    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userId", "user-123");
        variables.put("amount", 5000);
        variables.put("approved", true);
        context = new WorkflowContext("instance-1", "step-1", variables);
    }

    @Test
    @DisplayName("기본 속성 조회")
    void shouldReturnBasicProperties() {
        assertThat(context.getWorkflowInstanceId()).isEqualTo("instance-1");
        assertThat(context.getStepName()).isEqualTo("step-1");
    }

    @Test
    @DisplayName("변수 조회 - 제네릭 타입")
    void getVariable_ShouldReturnTypedValue() {
        String userId = context.getVariable("userId");
        Integer amount = context.getVariable("amount");
        Boolean approved = context.getVariable("approved");

        assertThat(userId).isEqualTo("user-123");
        assertThat(amount).isEqualTo(5000);
        assertThat(approved).isTrue();
    }

    @Test
    @DisplayName("변수 조회 - 기본값 포함")
    void getVariable_WithDefault_ShouldReturnDefaultIfMissing() {
        String value = context.getVariable("missing", "default-value");
        assertThat(value).isEqualTo("default-value");

        String existing = context.getVariable("userId", "fallback");
        assertThat(existing).isEqualTo("user-123");
    }

    @Test
    @DisplayName("변수 설정")
    void setVariable_ShouldStoreValue() {
        context.setVariable("newKey", "newValue");
        assertThat((String) context.getVariable("newKey")).isEqualTo("newValue");
    }

    @Test
    @DisplayName("변수 존재 여부")
    void hasVariable_ShouldCheckExistence() {
        assertThat(context.hasVariable("userId")).isTrue();
        assertThat(context.hasVariable("nonExistent")).isFalse();
    }

    @Test
    @DisplayName("변수 제거")
    void removeVariable_ShouldRemoveValue() {
        context.removeVariable("userId");
        assertThat(context.hasVariable("userId")).isFalse();
    }

    @Test
    @DisplayName("전체 변수 맵 조회 (읽기 전용)")
    void getVariables_ShouldReturnUnmodifiableMap() {
        Map<String, Object> variables = context.getVariables();
        assertThat(variables).containsKey("userId");
        assertThat(variables).containsKey("amount");
    }

    @Test
    @DisplayName("편의 메서드 - getString")
    void getString_ShouldReturnStringValue() {
        assertThat(context.getString("userId")).isEqualTo("user-123");
        assertThat(context.getString("amount")).isEqualTo("5000");
        assertThat(context.getString("nonExistent")).isNull();
    }

    @Test
    @DisplayName("편의 메서드 - getInteger")
    void getInteger_ShouldReturnIntegerValue() {
        assertThat(context.getInteger("amount")).isEqualTo(5000);
        assertThat(context.getInteger("userId")).isNull(); // String은 null 반환
        assertThat(context.getInteger("nonExistent")).isNull();
    }

    @Test
    @DisplayName("편의 메서드 - getBoolean")
    void getBoolean_ShouldReturnBooleanValue() {
        assertThat(context.getBoolean("approved")).isTrue();
        assertThat(context.getBoolean("userId")).isNull(); // String은 null 반환
        assertThat(context.getBoolean("nonExistent")).isNull();
    }

    @Test
    @DisplayName("null variables로 생성 시 빈 맵")
    void constructor_WithNullVariables_ShouldCreateEmptyMap() {
        WorkflowContext ctx = new WorkflowContext("id", "step", null);
        assertThat(ctx.getVariables()).isEmpty();
    }

    @Test
    @DisplayName("toString에 주요 정보 포함")
    void toString_ShouldContainKeyInfo() {
        String str = context.toString();
        assertThat(str).contains("instance-1");
        assertThat(str).contains("step-1");
    }
}
