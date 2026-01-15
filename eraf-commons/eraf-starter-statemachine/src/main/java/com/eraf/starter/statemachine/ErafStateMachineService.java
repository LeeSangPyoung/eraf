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
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    // 엔티티별 상태 저장 (실제 환경에서는 DB 또는 Redis로 대체)
    private final Map<String, StateInfo> stateStore = new ConcurrentHashMap<>();

    public ErafStateMachineService(ErafStateMachineRegistry registry, ApplicationEventPublisher eventPublisher) {
        this.registry = registry;
        this.eventPublisher = eventPublisher;
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

        String storeKey = createStoreKey(machineId, entityId);
        stateStore.put(storeKey, stateInfo);

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
        String storeKey = createStoreKey(machineId, entityId);
        return Optional.ofNullable(stateStore.get(storeKey));
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

        log.warn("State forced: machineId={}, entityId={}, {} -> {}",
                machineId, entityId, previousState, state);

        return stateInfo;
    }

    /**
     * 상태 삭제
     */
    public void removeState(String machineId, String entityId) {
        String storeKey = createStoreKey(machineId, entityId);
        stateStore.remove(storeKey);
    }

    private String createStoreKey(String machineId, String entityId) {
        return machineId + ":" + entityId;
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
