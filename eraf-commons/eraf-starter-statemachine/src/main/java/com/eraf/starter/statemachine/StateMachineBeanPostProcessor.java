package com.eraf.starter.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * @StateMachine 및 @Transition 어노테이션 처리를 위한 BeanPostProcessor
 */
public class StateMachineBeanPostProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StateMachineBeanPostProcessor.class);

    private final ErafStateMachineRegistry registry;

    public StateMachineBeanPostProcessor(ErafStateMachineRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();

        StateMachine annotation = targetClass.getAnnotation(StateMachine.class);
        if (annotation != null) {
            registerStateMachine(bean, annotation);
        }

        return bean;
    }

    private void registerStateMachine(Object bean, StateMachine annotation) {
        StateMachineDefinition definition = new StateMachineDefinition();
        definition.setId(annotation.id());
        definition.setInitialState(annotation.initialState());
        definition.setStates(new LinkedHashSet<>(Arrays.asList(annotation.states())));
        definition.setEndStates(new LinkedHashSet<>(Arrays.asList(annotation.endStates())));
        definition.setDescription(annotation.description());

        // 메소드에서 @Transition 어노테이션 스캔
        for (Method method : bean.getClass().getDeclaredMethods()) {
            Transition[] transitions = method.getAnnotationsByType(Transition.class);
            for (Transition transition : transitions) {
                TransitionInfo transitionInfo = new TransitionInfo();
                transitionInfo.setEvent(transition.event());
                transitionInfo.setSource(transition.source());
                transitionInfo.setTarget(transition.target());
                transitionInfo.setGuard(transition.guard());
                transitionInfo.setDescription(transition.description());
                transitionInfo.setAction(method);
                transitionInfo.setBean(bean);

                definition.addTransition(transitionInfo);

                log.debug("Registered transition: {} -> {} (event={})",
                        transition.source(), transition.target(), transition.event());
            }
        }

        registry.register(definition);
        log.info("Registered state machine: id={}, states={}, transitions={}",
                definition.getId(), definition.getStates().size(), definition.getTransitions().size());
    }
}
