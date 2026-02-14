package com.eraf.jpa.retention;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DataRetentionService 테스트
 */
@ExtendWith(MockitoExtension.class)
class DataRetentionServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Metamodel metamodel;

    @Mock
    private EntityType<?> entityType;

    @Mock
    private Query query;

    private DataRetentionService dataRetentionService;

    @BeforeEach
    void setUp() {
        dataRetentionService = new DataRetentionService(entityManager);
    }

    @Test
    void cleanupExpiredData_WithAnnotatedEntity_ShouldExecuteDelete() {
        // given
        @DataRetentionPolicy(days = 30, dateField = "createdAt")
        class TestEntity {}

        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn((Set) Set.of(entityType));
        when(entityType.getJavaType()).thenReturn((Class) TestEntity.class);
        when(entityType.getName()).thenReturn("TestEntity");
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(5);

        // when
        dataRetentionService.cleanupExpiredData();

        // then
        verify(entityManager).createQuery(contains("DELETE FROM TestEntity"));
        verify(query).setParameter(eq("cutoffDate"), any());
        verify(query).executeUpdate();
    }

    @Test
    void cleanupExpiredData_WithNoAnnotatedEntities_ShouldNotExecute() {
        // given
        class TestEntity {} // No annotation

        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn((Set) Set.of(entityType));
        when(entityType.getJavaType()).thenReturn((Class) TestEntity.class);

        // when
        dataRetentionService.cleanupExpiredData();

        // then
        verify(entityManager, never()).createQuery(anyString());
    }

    @Test
    void cleanupExpiredData_WithEmptyMetamodel_ShouldNotFail() {
        // given
        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(metamodel.getEntities()).thenReturn(Collections.emptySet());

        // when/then
        dataRetentionService.cleanupExpiredData();

        // Verify no exceptions thrown
        verify(entityManager, never()).createQuery(anyString());
    }
}
