package com.eraf.workflow.repository;

import com.eraf.workflow.WorkflowInstance;
import com.eraf.workflow.WorkflowStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory Workflow Repository 구현
 */
public class InMemoryWorkflowRepository implements WorkflowRepository {

    private final Map<String, WorkflowInstance> store = new ConcurrentHashMap<>();

    @Override
    public void save(WorkflowInstance instance) {
        store.put(instance.getId(), instance);
    }

    @Override
    public Optional<WorkflowInstance> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<WorkflowInstance> findByWorkflowNameAndStatus(String workflowName, WorkflowStatus status) {
        return store.values().stream()
                .filter(i -> i.getWorkflowName().equals(workflowName) && i.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> findByStatus(WorkflowStatus status) {
        return store.values().stream()
                .filter(i -> i.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowInstance> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void cleanupOlderThan(long ageMillis) {
        LocalDateTime cutoff = LocalDateTime.now().minus(Duration.ofMillis(ageMillis));
        store.entrySet().removeIf(entry -> {
            WorkflowInstance instance = entry.getValue();
            return instance.isTerminal() && instance.getUpdatedAt().isBefore(cutoff);
        });
    }
}
