package com.eraf.starter.statemachine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

/**
 * JPA/JDBC 상태 저장소 (영속성이 필요한 경우)
 * 상태 이력 관리가 필요한 경우 사용
 */
public class JpaStateStore implements StateStore {

    private static final Logger log = LoggerFactory.getLogger(JpaStateStore.class);
    private static final String TABLE_NAME = "eraf_statemachine_state";

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    private final boolean autoCreateTable;

    public JpaStateStore(DataSource dataSource) {
        this(dataSource, true);
    }

    public JpaStateStore(DataSource dataSource, boolean autoCreateTable) {
        this.dataSource = dataSource;
        this.autoCreateTable = autoCreateTable;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        if (autoCreateTable) {
            createTableIfNotExists();
        }
    }

    @Override
    public void save(String machineId, String entityId, StateInfo stateInfo) {
        String key = createKey(machineId, entityId);

        if (exists(machineId, entityId)) {
            update(key, stateInfo);
        } else {
            insert(key, stateInfo);
        }
    }

    @Override
    public Optional<StateInfo> find(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        String sql = "SELECT machine_id, entity_id, current_state, previous_state, " +
                "state_changed_at, context FROM " + TABLE_NAME + " WHERE store_key = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    StateInfo stateInfo = new StateInfo();
                    stateInfo.setMachineId(rs.getString("machine_id"));
                    stateInfo.setEntityId(rs.getString("entity_id"));
                    stateInfo.setCurrentState(rs.getString("current_state"));
                    stateInfo.setPreviousState(rs.getString("previous_state"));

                    Timestamp timestamp = rs.getTimestamp("state_changed_at");
                    if (timestamp != null) {
                        stateInfo.setStateChangedAt(timestamp.toInstant());
                    }

                    String contextJson = rs.getString("context");
                    if (contextJson != null && !contextJson.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> context = objectMapper.readValue(contextJson, java.util.Map.class);
                        stateInfo.setContext(context);
                    }

                    return Optional.of(stateInfo);
                }
            }
            return Optional.empty();

        } catch (SQLException | JsonProcessingException e) {
            log.error("Failed to find state: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void remove(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE store_key = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            stmt.executeUpdate();
            log.debug("State removed from DB: key={}", key);

        } catch (SQLException e) {
            log.error("Failed to remove state: {}", e.getMessage());
            throw new StateMachineException("Failed to remove state from DB", e);
        }
    }

    @Override
    public boolean exists(String machineId, String entityId) {
        String key = createKey(machineId, entityId);
        String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE store_key = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            log.error("Failed to check state existence: {}", e.getMessage());
            return false;
        }
    }

    private void insert(String key, StateInfo stateInfo) {
        String sql = "INSERT INTO " + TABLE_NAME +
                " (store_key, machine_id, entity_id, current_state, previous_state, state_changed_at, context, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp now = Timestamp.from(Instant.now());

            stmt.setString(1, key);
            stmt.setString(2, stateInfo.getMachineId());
            stmt.setString(3, stateInfo.getEntityId());
            stmt.setString(4, stateInfo.getCurrentState());
            stmt.setString(5, stateInfo.getPreviousState());
            stmt.setTimestamp(6, stateInfo.getStateChangedAt() != null ?
                    Timestamp.from(stateInfo.getStateChangedAt()) : now);
            stmt.setString(7, objectMapper.writeValueAsString(stateInfo.getContext()));
            stmt.setTimestamp(8, now);
            stmt.setTimestamp(9, now);

            stmt.executeUpdate();
            log.debug("State inserted to DB: key={}", key);

        } catch (SQLException | JsonProcessingException e) {
            log.error("Failed to insert state: {}", e.getMessage());
            throw new StateMachineException("Failed to save state to DB", e);
        }
    }

    private void update(String key, StateInfo stateInfo) {
        String sql = "UPDATE " + TABLE_NAME +
                " SET current_state = ?, previous_state = ?, state_changed_at = ?, context = ?, updated_at = ? " +
                "WHERE store_key = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            Timestamp now = Timestamp.from(Instant.now());

            stmt.setString(1, stateInfo.getCurrentState());
            stmt.setString(2, stateInfo.getPreviousState());
            stmt.setTimestamp(3, stateInfo.getStateChangedAt() != null ?
                    Timestamp.from(stateInfo.getStateChangedAt()) : now);
            stmt.setString(4, objectMapper.writeValueAsString(stateInfo.getContext()));
            stmt.setTimestamp(5, now);
            stmt.setString(6, key);

            stmt.executeUpdate();
            log.debug("State updated in DB: key={}", key);

        } catch (SQLException | JsonProcessingException e) {
            log.error("Failed to update state: {}", e.getMessage());
            throw new StateMachineException("Failed to update state in DB", e);
        }
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    store_key VARCHAR(255) NOT NULL UNIQUE,
                    machine_id VARCHAR(100) NOT NULL,
                    entity_id VARCHAR(255) NOT NULL,
                    current_state VARCHAR(100) NOT NULL,
                    previous_state VARCHAR(100),
                    state_changed_at TIMESTAMP,
                    context TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_machine_entity (machine_id, entity_id),
                    INDEX idx_current_state (current_state)
                )
                """.formatted(TABLE_NAME);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            log.info("State machine table created or already exists: {}", TABLE_NAME);

        } catch (SQLException e) {
            log.warn("Failed to create table (may already exist with different DDL): {}", e.getMessage());
        }
    }
}
