package com.eraf.saga.core;

import com.eraf.saga.annotation.Compensate;
import com.eraf.saga.annotation.Saga;
import com.eraf.saga.annotation.SagaStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Saga Bean Post Processor
 * @Saga 어노테이션이 붙은 빈을 자동으로 등록합니다.
 */
public class SagaRegistrar implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(SagaRegistrar.class);

    private final SagaOrchestrator orchestrator;

    public SagaRegistrar(SagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Saga sagaAnnotation = beanClass.getAnnotation(Saga.class);

        if (sagaAnnotation != null) {
            registerSaga(bean, beanClass, sagaAnnotation);
        }

        return bean;
    }

    private void registerSaga(Object bean, Class<?> beanClass, Saga sagaAnnotation) {
        SagaDefinition definition = new SagaDefinition(sagaAnnotation.name(), beanClass);
        definition.setSagaInstance(bean);
        definition.setDescription(sagaAnnotation.description());
        definition.setTimeout(sagaAnnotation.timeout());
        definition.setMaxRetries(sagaAnnotation.maxRetries());

        // Compensate 메서드 맵 구성
        Map<String, Method> compensateMethods = new HashMap<>();
        for (Method method : beanClass.getDeclaredMethods()) {
            Compensate compensate = method.getAnnotation(Compensate.class);
            if (compensate != null) {
                String key = compensate.value().isEmpty() ? method.getName() : compensate.value();
                compensateMethods.put(key, method);
            }
        }

        // Step 등록
        for (Method method : beanClass.getDeclaredMethods()) {
            SagaStep stepAnnotation = method.getAnnotation(SagaStep.class);
            if (stepAnnotation != null) {
                String stepName = stepAnnotation.name().isEmpty() ? method.getName() : stepAnnotation.name();

                SagaDefinition.StepDefinition step = new SagaDefinition.StepDefinition(
                        stepAnnotation.order(),
                        stepName,
                        method
                );
                step.setTimeout(stepAnnotation.timeout());
                step.setRetries(stepAnnotation.retries());
                step.setRetryDelay(stepAnnotation.retryDelay());

                // Compensate 메서드 찾기
                if (!stepAnnotation.compensate().isEmpty()) {
                    Method compensateMethod = compensateMethods.get(stepAnnotation.compensate());
                    if (compensateMethod == null) {
                        // 메서드 이름으로 직접 찾기
                        try {
                            compensateMethod = findCompensateMethod(beanClass, stepAnnotation.compensate());
                        } catch (NoSuchMethodException e) {
                            log.warn("Compensate method not found: {} for step {}",
                                    stepAnnotation.compensate(), stepName);
                        }
                    }
                    step.setCompensateMethod(compensateMethod);
                }

                definition.addStep(step);
            }
        }

        if (definition.getTotalSteps() > 0) {
            orchestrator.register(definition);
            log.info("Registered saga '{}' with {} steps from class {}",
                    sagaAnnotation.name(), definition.getTotalSteps(), beanClass.getSimpleName());
        } else {
            log.warn("Saga '{}' has no steps defined", sagaAnnotation.name());
        }
    }

    private Method findCompensateMethod(Class<?> beanClass, String methodName) throws NoSuchMethodException {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}
