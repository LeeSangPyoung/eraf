package com.eraf.saga.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SagaDefinitionTest {

    private SagaDefinition definition;

    @BeforeEach
    void setUp() {
        definition = new SagaDefinition("testSaga", TestSaga.class);
    }

    @Test
    @DisplayName("기본 생성자")
    void testDefaultConstructor() {
        // Given
        SagaDefinition defaultDef = new SagaDefinition();

        // Then
        assertNotNull(defaultDef.getSteps());
        assertTrue(defaultDef.getSteps().isEmpty());
    }

    @Test
    @DisplayName("이름과 클래스로 생성")
    void testConstructorWithNameAndClass() {
        // Then
        assertEquals("testSaga", definition.getName());
        assertEquals(TestSaga.class, definition.getSagaClass());
    }

    @Test
    @DisplayName("Step 추가 및 정렬")
    void testAddStepAndSort() throws NoSuchMethodException {
        // Given
        Method method = TestSaga.class.getMethod("step1");
        SagaDefinition.StepDefinition step3 = new SagaDefinition.StepDefinition(3, "step3", method);
        SagaDefinition.StepDefinition step1 = new SagaDefinition.StepDefinition(1, "step1", method);
        SagaDefinition.StepDefinition step2 = new SagaDefinition.StepDefinition(2, "step2", method);

        // When
        definition.addStep(step3);
        definition.addStep(step1);
        definition.addStep(step2);

        // Then
        assertEquals(3, definition.getSteps().size());
        assertEquals(1, definition.getSteps().get(0).getOrder());
        assertEquals(2, definition.getSteps().get(1).getOrder());
        assertEquals(3, definition.getSteps().get(2).getOrder());
    }

    @Test
    @DisplayName("순서로 Step 조회")
    void testGetStep() throws NoSuchMethodException {
        // Given
        Method method = TestSaga.class.getMethod("step1");
        definition.addStep(new SagaDefinition.StepDefinition(1, "step1", method));
        definition.addStep(new SagaDefinition.StepDefinition(2, "step2", method));

        // When
        SagaDefinition.StepDefinition step = definition.getStep(1);

        // Then
        assertNotNull(step);
        assertEquals("step1", step.getName());
    }

    @Test
    @DisplayName("존재하지 않는 순서로 Step 조회")
    void testGetStepNotFound() {
        // When
        SagaDefinition.StepDefinition step = definition.getStep(99);

        // Then
        assertNull(step);
    }

    @Test
    @DisplayName("이름으로 Step 조회")
    void testGetStepByName() throws NoSuchMethodException {
        // Given
        Method method = TestSaga.class.getMethod("step1");
        definition.addStep(new SagaDefinition.StepDefinition(1, "findMe", method));

        // When
        SagaDefinition.StepDefinition step = definition.getStepByName("findMe");

        // Then
        assertNotNull(step);
        assertEquals(1, step.getOrder());
    }

    @Test
    @DisplayName("존재하지 않는 이름으로 Step 조회")
    void testGetStepByNameNotFound() {
        // When
        SagaDefinition.StepDefinition step = definition.getStepByName("nonexistent");

        // Then
        assertNull(step);
    }

    @Test
    @DisplayName("전체 Step 수 조회")
    void testGetTotalSteps() throws NoSuchMethodException {
        // Given
        Method method = TestSaga.class.getMethod("step1");
        definition.addStep(new SagaDefinition.StepDefinition(1, "step1", method));
        definition.addStep(new SagaDefinition.StepDefinition(2, "step2", method));

        // Then
        assertEquals(2, definition.getTotalSteps());
    }

    @Test
    @DisplayName("Setter 동작 확인")
    void testSetters() {
        // Given
        Object instance = new TestSaga();

        // When
        definition.setName("newName");
        definition.setDescription("Test description");
        definition.setSagaClass(String.class);
        definition.setSagaInstance(instance);
        definition.setTimeout(5000);
        definition.setMaxRetries(3);

        // Then
        assertEquals("newName", definition.getName());
        assertEquals("Test description", definition.getDescription());
        assertEquals(String.class, definition.getSagaClass());
        assertEquals(instance, definition.getSagaInstance());
        assertEquals(5000, definition.getTimeout());
        assertEquals(3, definition.getMaxRetries());
    }

    @Test
    @DisplayName("StepDefinition 기본 생성자")
    void testStepDefinitionDefaultConstructor() {
        // Given
        SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition();

        // Then
        assertEquals(0, step.getOrder());
        assertNull(step.getName());
        assertNull(step.getMethod());
        assertFalse(step.hasCompensation());
    }

    @Test
    @DisplayName("StepDefinition Setter 동작 확인")
    void testStepDefinitionSetters() throws NoSuchMethodException {
        // Given
        SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition();
        Method method = TestSaga.class.getMethod("step1");
        Method compensateMethod = TestSaga.class.getMethod("compensateStep1");

        // When
        step.setOrder(5);
        step.setName("testStep");
        step.setMethod(method);
        step.setCompensateMethod(compensateMethod);
        step.setTimeout(3000);
        step.setRetries(2);
        step.setRetryDelay(1000);

        // Then
        assertEquals(5, step.getOrder());
        assertEquals("testStep", step.getName());
        assertEquals(method, step.getMethod());
        assertEquals(compensateMethod, step.getCompensateMethod());
        assertEquals(3000, step.getTimeout());
        assertEquals(2, step.getRetries());
        assertEquals(1000, step.getRetryDelay());
        assertTrue(step.hasCompensation());
    }

    @Test
    @DisplayName("hasCompensation - 보상 메서드 없음")
    void testHasCompensationFalse() {
        // Given
        SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition();

        // Then
        assertFalse(step.hasCompensation());
    }

    @Test
    @DisplayName("hasCompensation - 보상 메서드 있음")
    void testHasCompensationTrue() throws NoSuchMethodException {
        // Given
        SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition();
        step.setCompensateMethod(TestSaga.class.getMethod("compensateStep1"));

        // Then
        assertTrue(step.hasCompensation());
    }

    // Test Saga class
    public static class TestSaga {
        public void step1() {}
        public void compensateStep1() {}
    }
}
