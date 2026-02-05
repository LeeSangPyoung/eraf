package com.eraf.sample.controller.statemachine;

import com.eraf.statemachine.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 상태 머신 컨트롤러 (#29~#34)
 *
 * #29 주문 상태 머신 정의
 * #30 상태 전이 시각화
 * #31 현재 상태 조회
 * #32 이벤트 전송
 * #33 상태 변경 이력 조회
 * #34 강제 상태 변경 (관리자)
 */
@Slf4j
@Controller
@RequestMapping("/statemachine")
public class StateMachineController {

    private final ErafStateMachineRegistry registry;
    private final ErafStateMachineService stateMachineService;

    // 상태 변경 이력 저장 (데모용)
    private final List<StateHistoryEntry> stateHistory = new CopyOnWriteArrayList<>();

    public StateMachineController(ApplicationEventPublisher eventPublisher) {
        this.registry = new ErafStateMachineRegistry();
        this.stateMachineService = new ErafStateMachineService(registry, eventPublisher);

        // 상태 머신 정의 등록
        initializeStateMachines();
    }

    private void initializeStateMachines() {
        // #29 주문 상태 머신 정의
        StateMachineDefinition orderMachine = createOrderStateMachine();
        registry.register(orderMachine);

        // 배송 상태 머신 정의
        StateMachineDefinition deliveryMachine = createDeliveryStateMachine();
        registry.register(deliveryMachine);

        // 결제 상태 머신 정의
        StateMachineDefinition paymentMachine = createPaymentStateMachine();
        registry.register(paymentMachine);
    }

    private StateMachineDefinition createOrderStateMachine() {
        StateMachineDefinition def = new StateMachineDefinition();
        def.setId("order");
        def.setDescription("Order Processing State Machine");
        def.setInitialState("CREATED");
        def.setStates(new LinkedHashSet<>(List.of(
                "CREATED", "PENDING_PAYMENT", "PAID", "PROCESSING",
                "SHIPPED", "DELIVERED", "COMPLETED", "CANCELLED", "REFUNDED"
        )));
        def.setEndStates(new LinkedHashSet<>(List.of("COMPLETED", "CANCELLED", "REFUNDED")));

        // 전이 정의
        def.addTransition(createTransition("CREATED", "SUBMIT", "PENDING_PAYMENT"));
        def.addTransition(createTransition("CREATED", "CANCEL", "CANCELLED"));
        def.addTransition(createTransition("PENDING_PAYMENT", "PAY", "PAID"));
        def.addTransition(createTransition("PENDING_PAYMENT", "CANCEL", "CANCELLED"));
        def.addTransition(createTransition("PENDING_PAYMENT", "TIMEOUT", "CANCELLED"));
        def.addTransition(createTransition("PAID", "PROCESS", "PROCESSING"));
        def.addTransition(createTransition("PAID", "REFUND", "REFUNDED"));
        def.addTransition(createTransition("PROCESSING", "SHIP", "SHIPPED"));
        def.addTransition(createTransition("PROCESSING", "CANCEL", "CANCELLED"));
        def.addTransition(createTransition("SHIPPED", "DELIVER", "DELIVERED"));
        def.addTransition(createTransition("DELIVERED", "COMPLETE", "COMPLETED"));
        def.addTransition(createTransition("DELIVERED", "RETURN", "REFUNDED"));

        return def;
    }

    private StateMachineDefinition createDeliveryStateMachine() {
        StateMachineDefinition def = new StateMachineDefinition();
        def.setId("delivery");
        def.setDescription("Delivery State Machine");
        def.setInitialState("PENDING");
        def.setStates(new LinkedHashSet<>(List.of(
                "PENDING", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED", "FAILED"
        )));
        def.setEndStates(new LinkedHashSet<>(List.of("DELIVERED", "FAILED")));

        def.addTransition(createTransition("PENDING", "PICKUP", "PICKED_UP"));
        def.addTransition(createTransition("PICKED_UP", "TRANSIT", "IN_TRANSIT"));
        def.addTransition(createTransition("IN_TRANSIT", "OUT_DELIVERY", "OUT_FOR_DELIVERY"));
        def.addTransition(createTransition("OUT_FOR_DELIVERY", "DELIVER", "DELIVERED"));
        def.addTransition(createTransition("OUT_FOR_DELIVERY", "FAIL", "FAILED"));
        def.addTransition(createTransition("FAILED", "RETRY", "OUT_FOR_DELIVERY"));

        return def;
    }

    private StateMachineDefinition createPaymentStateMachine() {
        StateMachineDefinition def = new StateMachineDefinition();
        def.setId("payment");
        def.setDescription("Payment State Machine");
        def.setInitialState("INITIATED");
        def.setStates(new LinkedHashSet<>(List.of(
                "INITIATED", "PROCESSING", "AUTHORIZED", "CAPTURED", "COMPLETED", "FAILED", "REFUNDED"
        )));
        def.setEndStates(new LinkedHashSet<>(List.of("COMPLETED", "FAILED", "REFUNDED")));

        def.addTransition(createTransition("INITIATED", "START", "PROCESSING"));
        def.addTransition(createTransition("PROCESSING", "AUTHORIZE", "AUTHORIZED"));
        def.addTransition(createTransition("PROCESSING", "FAIL", "FAILED"));
        def.addTransition(createTransition("AUTHORIZED", "CAPTURE", "CAPTURED"));
        def.addTransition(createTransition("AUTHORIZED", "CANCEL", "FAILED"));
        def.addTransition(createTransition("CAPTURED", "COMPLETE", "COMPLETED"));
        def.addTransition(createTransition("CAPTURED", "REFUND", "REFUNDED"));
        def.addTransition(createTransition("COMPLETED", "REFUND", "REFUNDED"));

        return def;
    }

    private TransitionInfo createTransition(String source, String event, String target) {
        TransitionInfo transition = new TransitionInfo();
        transition.setSource(source);
        transition.setEvent(event);
        transition.setTarget(target);
        return transition;
    }

    @GetMapping
    public String stateMachinePage(Model model) {
        model.addAttribute("machines", registry.getAllDefinitions());
        return "statemachine/statemachine";
    }

    // ===== #29 상태 머신 정의 조회 =====
    @GetMapping("/definitions")
    @ResponseBody
    public Map<String, Object> getDefinitions() {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> definitions = new ArrayList<>();
        for (StateMachineDefinition def : registry.getAllDefinitions()) {
            definitions.add(definitionToMap(def));
        }

        result.put("definitions", definitions);
        result.put("count", definitions.size());

        return result;
    }

    @GetMapping("/definition/{machineId}")
    @ResponseBody
    public Map<String, Object> getDefinition(@PathVariable String machineId) {
        return registry.getDefinition(machineId)
                .map(this::definitionToMap)
                .orElse(Map.of("error", "Machine not found: " + machineId));
    }

    // ===== #30 상태 전이 시각화 (다이어그램 데이터) =====
    @GetMapping("/diagram/{machineId}")
    @ResponseBody
    public Map<String, Object> getDiagram(@PathVariable String machineId) {
        Map<String, Object> result = new HashMap<>();

        StateMachineDefinition def = registry.getDefinition(machineId).orElse(null);
        if (def == null) {
            result.put("error", "Machine not found: " + machineId);
            return result;
        }

        // Nodes (states)
        List<Map<String, Object>> nodes = new ArrayList<>();
        int index = 0;
        Map<String, Integer> stateIndex = new HashMap<>();
        for (String state : def.getStates()) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", state);
            node.put("label", state);
            node.put("isInitial", state.equals(def.getInitialState()));
            node.put("isEnd", def.isEndState(state));
            stateIndex.put(state, index++);
            nodes.add(node);
        }

        // Edges (transitions)
        List<Map<String, Object>> edges = new ArrayList<>();
        for (TransitionInfo transition : def.getTransitions()) {
            Map<String, Object> edge = new HashMap<>();
            edge.put("from", transition.getSource());
            edge.put("to", transition.getTarget());
            edge.put("label", transition.getEvent());
            edges.add(edge);
        }

        result.put("machineId", machineId);
        result.put("description", def.getDescription());
        result.put("nodes", nodes);
        result.put("edges", edges);

        return result;
    }

    // ===== #31 현재 상태 조회 =====
    @GetMapping("/state/{machineId}/{entityId}")
    @ResponseBody
    public Map<String, Object> getState(@PathVariable String machineId, @PathVariable String entityId) {
        Map<String, Object> result = new HashMap<>();

        Optional<StateInfo> stateOpt = stateMachineService.getState(machineId, entityId);
        if (stateOpt.isPresent()) {
            result.put("found", true);
            result.put("state", stateInfoToMap(stateOpt.get()));

            // 가능한 이벤트 목록
            List<String> availableEvents = stateMachineService.getAvailableEvents(machineId, entityId);
            result.put("availableEvents", availableEvents);
            result.put("isEndState", stateMachineService.isInEndState(machineId, entityId));
        } else {
            result.put("found", false);
            result.put("message", "Entity not found. Initialize first.");
        }

        return result;
    }

    // ===== #31 상태 머신 초기화 =====
    @PostMapping("/initialize")
    @ResponseBody
    public Map<String, Object> initialize(@RequestBody InitializeRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            StateInfo stateInfo = stateMachineService.initialize(
                    request.machineId(),
                    request.entityId(),
                    request.context() != null ? request.context() : Map.of()
            );

            result.put("success", true);
            result.put("state", stateInfoToMap(stateInfo));

            // 이력 추가
            addHistory(request.machineId(), request.entityId(), null, stateInfo.getCurrentState(), "INITIALIZE");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #32 이벤트 전송 =====
    @PostMapping("/send-event")
    @ResponseBody
    public Map<String, Object> sendEvent(@RequestBody SendEventRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 현재 상태 조회
            String previousState = stateMachineService.getCurrentState(request.machineId(), request.entityId())
                    .orElse("UNKNOWN");

            StateInfo stateInfo = stateMachineService.sendEvent(
                    request.machineId(),
                    request.entityId(),
                    request.event(),
                    request.context() != null ? request.context() : Map.of()
            );

            result.put("success", true);
            result.put("previousState", previousState);
            result.put("currentState", stateInfo.getCurrentState());
            result.put("state", stateInfoToMap(stateInfo));

            // 가능한 다음 이벤트
            List<String> availableEvents = stateMachineService.getAvailableEvents(request.machineId(), request.entityId());
            result.put("availableEvents", availableEvents);
            result.put("isEndState", stateMachineService.isInEndState(request.machineId(), request.entityId()));

            // 이력 추가
            addHistory(request.machineId(), request.entityId(), previousState, stateInfo.getCurrentState(), request.event());

        } catch (StateMachineException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== #33 상태 변경 이력 조회 =====
    @GetMapping("/history/{machineId}/{entityId}")
    @ResponseBody
    public Map<String, Object> getHistory(@PathVariable String machineId, @PathVariable String entityId) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> history = stateHistory.stream()
                .filter(h -> h.machineId().equals(machineId) && h.entityId().equals(entityId))
                .sorted(Comparator.comparing(StateHistoryEntry::timestamp).reversed())
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", h.timestamp().toString());
                    map.put("previousState", h.previousState());
                    map.put("currentState", h.currentState());
                    map.put("event", h.event());
                    return map;
                })
                .toList();

        result.put("machineId", machineId);
        result.put("entityId", entityId);
        result.put("history", history);
        result.put("count", history.size());

        return result;
    }

    @GetMapping("/history/all")
    @ResponseBody
    public Map<String, Object> getAllHistory(@RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> history = stateHistory.stream()
                .sorted(Comparator.comparing(StateHistoryEntry::timestamp).reversed())
                .limit(limit)
                .map(h -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", h.timestamp().toString());
                    map.put("machineId", h.machineId());
                    map.put("entityId", h.entityId());
                    map.put("previousState", h.previousState());
                    map.put("currentState", h.currentState());
                    map.put("event", h.event());
                    return map;
                })
                .toList();

        result.put("history", history);
        result.put("total", stateHistory.size());

        return result;
    }

    // ===== #34 강제 상태 변경 (관리자) =====
    @PostMapping("/force-state")
    @ResponseBody
    public Map<String, Object> forceState(@RequestBody ForceStateRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            String previousState = stateMachineService.getCurrentState(request.machineId(), request.entityId())
                    .orElse("UNKNOWN");

            StateInfo stateInfo = stateMachineService.forceState(
                    request.machineId(),
                    request.entityId(),
                    request.targetState()
            );

            result.put("success", true);
            result.put("previousState", previousState);
            result.put("currentState", stateInfo.getCurrentState());
            result.put("warning", "State was forcefully changed by administrator");

            // 이력 추가
            addHistory(request.machineId(), request.entityId(), previousState, stateInfo.getCurrentState(), "FORCE_CHANGE");

        } catch (StateMachineException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // ===== 엔티티 목록 조회 (데모용) =====
    @GetMapping("/entities/{machineId}")
    @ResponseBody
    public Map<String, Object> getEntities(@PathVariable String machineId) {
        Map<String, Object> result = new HashMap<>();

        // 이력에서 해당 머신의 엔티티 추출
        Set<String> entityIds = new LinkedHashSet<>();
        for (StateHistoryEntry entry : stateHistory) {
            if (entry.machineId().equals(machineId)) {
                entityIds.add(entry.entityId());
            }
        }

        List<Map<String, Object>> entities = new ArrayList<>();
        for (String entityId : entityIds) {
            Optional<StateInfo> stateOpt = stateMachineService.getState(machineId, entityId);
            if (stateOpt.isPresent()) {
                StateInfo state = stateOpt.get();
                entities.add(Map.of(
                        "entityId", entityId,
                        "currentState", state.getCurrentState(),
                        "stateChangedAt", state.getStateChangedAt() != null ? state.getStateChangedAt().toString() : ""
                ));
            }
        }

        result.put("machineId", machineId);
        result.put("entities", entities);
        result.put("count", entities.size());

        return result;
    }

    // ===== Event Listener =====
    @EventListener
    public void onStateChange(StateChangeEvent event) {
        log.info("State changed: machineId={}, entityId={}, {} -> {} (event={})",
                event.getMachineId(), event.getEntityId(),
                event.getFromState(), event.getToState(), event.getEvent());
    }

    // ===== Helper Methods =====
    private Map<String, Object> definitionToMap(StateMachineDefinition def) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", def.getId());
        map.put("description", def.getDescription());
        map.put("initialState", def.getInitialState());
        map.put("states", new ArrayList<>(def.getStates()));
        map.put("endStates", new ArrayList<>(def.getEndStates()));

        List<Map<String, String>> transitions = def.getTransitions().stream()
                .map(t -> Map.of(
                        "source", t.getSource(),
                        "event", t.getEvent(),
                        "target", t.getTarget()
                ))
                .toList();
        map.put("transitions", transitions);

        return map;
    }

    private Map<String, Object> stateInfoToMap(StateInfo info) {
        Map<String, Object> map = new HashMap<>();
        map.put("machineId", info.getMachineId());
        map.put("entityId", info.getEntityId());
        map.put("currentState", info.getCurrentState());
        map.put("previousState", info.getPreviousState());
        map.put("stateChangedAt", info.getStateChangedAt() != null ? info.getStateChangedAt().toString() : null);
        map.put("context", info.getContext());
        return map;
    }

    private void addHistory(String machineId, String entityId, String previousState, String currentState, String event) {
        stateHistory.add(new StateHistoryEntry(
                Instant.now(), machineId, entityId, previousState, currentState, event
        ));
    }

    // ===== Request DTOs =====
    record InitializeRequest(String machineId, String entityId, Map<String, Object> context) {}
    record SendEventRequest(String machineId, String entityId, String event, Map<String, Object> context) {}
    record ForceStateRequest(String machineId, String entityId, String targetState) {}
    record StateHistoryEntry(Instant timestamp, String machineId, String entityId, String previousState, String currentState, String event) {}
}
