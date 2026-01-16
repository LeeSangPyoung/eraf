package com.eraf.starter.statemachine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ERAF 상태 머신 서비스
 * 상태 전이 관리 및 실행
 */
public class ErafStateMachineService {

    private static final Logger log = LoggerFactory.getLogger(ErafStateMachineService.class);

    private final ErafStateMachineRegistry registry;
    private final ApplicationEventPublisher eventPublisher;
    private final StateStore stateStore;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * 기본 생성자 (InMemory StateStore 사용)
     */
    public ErafStateMachineService(ErafStateMachineRegistry registry, ApplicationEventPublisher eventPublisher) {
        this(registry, eventPublisher, new InMemoryStateStore());
    }

    /**
     * StateStore를 지정하는 생성자 (분산 환경용)
     */
    public ErafStateMachineService(ErafStateMachineRegistry registry, ApplicationEventPublisher eventPublisher, StateStore stateStore) {
        this.registry = registry;
        this.eventPublisher = eventPublisher;
        this.stateStore = stateStore;
        log.info("ErafStateMachineService initialized with store: {}", stateStore.getClass().getSimpleName());
    }

    /**
     * 상태 머신 초기화
     */
    public StateInfo initialize(String machineId, String entityId) {
        return initialize(machineId, entityId, Map.of());
    }

    /**
     * 상태 머신 초기화 (컨텍스트 포함)
     */
    public StateInfo initialize(String machineId, String entityId, Map<String, Object> context) {
        StateMachineDefinition definition = registry.getDefinition(machineId)
                .orElseThrow(() -> StateMachineException.machineNotFound(machineId));

        StateInfo stateInfo = new StateInfo();
        stateInfo.setMachineId(machineId);
        stateInfo.setEntityId(entityId);
        stateInfo.setCurrentState(definition.getInitialState());
        stateInfo.setStateChangedAt(Instant.now());
        stateInfo.setContext(new ConcurrentHashMap<>(context));

        stateStore.save(machineId, entityId, stateInfo);

        log.info("State machine initialized: machineId={}, entityId={}, initialState={}",
                machineId, entityId, definition.getInitialState());

        return stateInfo;
    }

    /**
     * 이벤트 전송 및 상태 전이
     */
    public StateInfo sendEvent(String machineId, String entityId, String event) {
        return sendEvent(machineId, entityId, event, Map.of());
    }

    /**
     * 이벤트 전송 및 상태 전이 (컨텍스트 포함)
     */
    public StateInfo sendEvent(String machineId, String entityId, String event, Map<String, Object> eventContext) {
        StateMachineDefinition definition = registry.getDefinition(machineId)
                .orElseThrow(() -> StateMachineException.machineNotFound(machineId));

        StateInfo stateInfo = getState(machineId, entityId)
                .orElseThrow(() -> new StateMachineException("State not found for entity: " + entityId));

        String currentState = stateInfo.getCurrentState();

        // 종료 상태에서는 전이 불가
        if (definition.isEndState(currentState)) {
            throw new StateMachineException("Cannot transition from end state: " + currentState);
        }

        // 전이 정보 조회
        TransitionInfo transition = definition.findTransition(currentState, event)
                .orElseThrow(() -> StateMachineException.invalidTransition(machineId, currentState, event));

        // 가드 조건 평가
        if (!evaluateGuard(transition, stateInfo, eventContext)) {
            throw StateMachineException.guardFailed(machineId, currentState, event, transition.getGuard());
        }

        // 액션 실행
        executeAction(transition, stateInfo, eventContext);

        // 상태 변경
        String previousState = stateInfo.getCurrentState();
        stateInfo.setPreviousState(previousState);
        stateInfo.setCurrentState(transition.getTarget());
        stateInfo.setStateChangedAt(Instant.now());

        // 이벤트 컨텍스트 병합
        stateInfo.getContext().putAll(eventContext);

        // 상태 저장
        stateStore.save(machineId, entityId, stateInfo);

        log.info("State transition: machineId={}, entityId={}, {} -> {} (event={})",
                machineId, entityId, previousState, transition.getTarget(), event);

        // 상태 변경 이벤트 발행
        eventPublisher.publishEvent(new StateChangeEvent(
                this, machineId, entityId, previousState, transition.getTarget(), event, stateInfo.getContext()
        ));

        return stateInfo;
    }

    /**
     * 현재 상태 조회
     */
    public Optional<StateInfo> getState(String machineId, String entityId) {
        return stateStore.find(machineId, entityId);
    }

    /**
     * 현재 상태 문자열 조회
     */
    public Optional<String> getCurrentState(String machineId, String entityId) {
        return getState(machineId, entityId).map(StateInfo::getCurrentState);
    }

    /**
     * 현재 상태에서 가능한 이벤트 목록 조회
     */
    public List<String> getAvailableEvents(String machineId, String entityId) {
        StateMachineDefinition definition = registry.getDefinition(machineId)
                .orElseThrow(() -> StateMachineException.machineNotFound(machineId));

        String currentState = getCurrentState(machineId, entityId)
                .orElseThrow(() -> new StateMachineException("State not found for entity: " + entityId));

        return definition.getTransitionsFrom(currentState).stream()
                .map(TransitionInfo::getEvent)
                .toList();
    }

    /**
     * 특정 이벤트로 전이 가능 여부 확인
     */
    public boolean canSendEvent(String machineId, String entityId, String event) {
        try {
            StateMachineDefinition definition = registry.getDefinition(machineId).orElse(null);
            if (definition == null) return false;

            StateInfo stateInfo = getState(machineId, entityId).orElse(null);
            if (stateInfo == null) return false;

            String currentState = stateInfo.getCurrentState();
            if (definition.isEndState(currentState)) return false;

            TransitionInfo transition = definition.findTransition(currentState, event).orElse(null);
            if (transition == null) return false;

            return evaluateGuard(transition, stateInfo, Map.of());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 종료 상태 여부 확인
     */
    public boolean isInEndState(String machineId, String entityId) {
        StateMachineDefinition definition = registry.getDefinition(machineId)
                .orElseThrow(() -> StateMachineException.machineNotFound(machineId));

        String currentState = getCurrentState(machineId, entityId)
                .orElseThrow(() -> new StateMachineException("State not found for entity: " + entityId));

        return definition.isEndState(currentState);
    }

    /**
     * 상태 강제 설정 (관리자용)
     */
    public StateInfo forceState(String machineId, String entityId, String state) {
        StateMachineDefinition definition = registry.getDefinition(machineId)
                .orElseThrow(() -> StateMachineException.machineNotFound(machineId));

        if (!definition.isValidState(state)) {
            throw StateMachineException.invalidState(machineId, state);
        }

        StateInfo stateInfo = getState(machineId, entityId)
                .orElseThrow(() -> new StateMachineException("State not found for entity: " + entityId));

        String previousState = stateInfo.getCurrentState();
        stateInfo.setPreviousState(previousState);
        stateInfo.setCurrentState(state);
        stateInfo.setStateChangedAt(Instant.now());

        // 상태 저장
        stateStore.save(machineId, entityId, stateInfo);

        log.warn("State forced: machineId={}, entityId={}, {} -> {}",
                machineId, entityId, previousState, state);

        return stateInfo;
    }

    /**
     * 상태 삭제
     */
    public void removeState(String machineId, String entityId) {
        stateStore.remove(machineId, entityId);
    }

    /**
     * 상태 존재 여부 확인
     */
    public boolean hasState(String machineId, String entityId) {
        return stateStore.exists(machineId, entityId);
    }

    /**
     * 사용 중인 StateStore 반환
     */
    public StateStore getStateStore() {
        return stateStore;
    }

    private boolean evaluateGuard(TransitionInfo transition, StateInfo stateInfo, Map<String, Object> eventContext) {
        String guard = transition.getGuard();
        if (guard == null || guard.isEmpty()) {
            return true;
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("state", stateInfo);
            context.setVariable("context", stateInfo.getContext());
            context.setVariable("event", eventContext);

            Boolean result = expressionParser.parseExpression(guard).getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.error("Guard evaluation failed: {}", guard, e);
            return false;
        }
    }

    private void executeAction(TransitionInfo transition, StateInfo stateInfo, Map<String, Object> eventContext) {
        Method action = transition.getAction();
        Object bean = transition.getBean();

        if (action == null || bean == null) {
            return;
        }

        try {
            Class<?>[] paramTypes = action.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                if (StateInfo.class.isAssignableFrom(paramTypes[i])) {
                    args[i] = stateInfo;
                } else if (Map.class.isAssignableFrom(paramTypes[i])) {
                    args[i] = eventContext;
                }
            }

            action.setAccessible(true);
            action.invoke(bean, args);
        } catch (Exception e) {
            log.error("Action execution failed: {}", action.getName(), e);
            throw new StateMachineException("Action execution failed: " + action.getName(), e);
        }
    }
}
