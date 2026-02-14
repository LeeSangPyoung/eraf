package com.eraf.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

/**
 * Circuit Breaker Registry Unit Tests
 */
class CircuitBreakerRegistryTest {

    private CircuitBreakerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CircuitBreakerRegistry(5, 3, 30000);
    }

    @Test
    void shouldCreateCircuitBreakerWithDefaultSettings() {
        // when
        CircuitBreaker cb = registry.getOrCreate("test");

        // then
        assertThat(cb).isNotNull();
        assertThat(cb.getName()).isEqualTo("test");
        assertThat(cb.getFailureThreshold()).isEqualTo(5);
        assertThat(cb.getSuccessThreshold()).isEqualTo(3);
        assertThat(cb.getOpenTimeoutMs()).isEqualTo(30000);
    }

    @Test
    void shouldReturnSameInstanceForSameName() {
        // when
        CircuitBreaker cb1 = registry.getOrCreate("test");
        CircuitBreaker cb2 = registry.getOrCreate("test");

        // then
        assertThat(cb1).isSameAs(cb2);
    }

    @Test
    void shouldCreateCircuitBreakerWithCustomSettings() {
        // when
        CircuitBreaker cb = registry.getOrCreate("custom", 10, 5, 60000);

        // then
        assertThat(cb.getName()).isEqualTo("custom");
        assertThat(cb.getFailureThreshold()).isEqualTo(10);
        assertThat(cb.getSuccessThreshold()).isEqualTo(5);
        assertThat(cb.getOpenTimeoutMs()).isEqualTo(60000);
    }

    @Test
    void shouldGetExistingCircuitBreaker() {
        // given
        registry.getOrCreate("test");

        // when
        CircuitBreaker cb = registry.get("test");

        // then
        assertThat(cb).isNotNull();
        assertThat(cb.getName()).isEqualTo("test");
    }

    @Test
    void shouldReturnNullForNonExistentCircuitBreaker() {
        // when
        CircuitBreaker cb = registry.get("nonexistent");

        // then
        assertThat(cb).isNull();
    }

    @Test
    void shouldRemoveCircuitBreaker() {
        // given
        registry.getOrCreate("test");
        assertThat(registry.contains("test")).isTrue();

        // when
        CircuitBreaker removed = registry.remove("test");

        // then
        assertThat(removed).isNotNull();
        assertThat(registry.contains("test")).isFalse();
        assertThat(registry.get("test")).isNull();
    }

    @Test
    void shouldCheckContains() {
        // given
        registry.getOrCreate("test");

        // when & then
        assertThat(registry.contains("test")).isTrue();
        assertThat(registry.contains("nonexistent")).isFalse();
    }

    @Test
    void shouldReturnAllCircuitBreakers() {
        // given
        registry.getOrCreate("cb1");
        registry.getOrCreate("cb2");
        registry.getOrCreate("cb3");

        // when
        Collection<CircuitBreaker> all = registry.getAll();

        // then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(CircuitBreaker::getName)
                .containsExactlyInAnyOrder("cb1", "cb2", "cb3");
    }

    @Test
    void shouldResetAllCircuitBreakers() {
        // given
        CircuitBreaker cb1 = registry.getOrCreate("cb1");
        CircuitBreaker cb2 = registry.getOrCreate("cb2");
        cb1.recordFailure();
        cb1.recordFailure();
        cb2.recordFailure();

        // when
        registry.resetAll();

        // then
        assertThat(cb1.getFailureCount()).isZero();
        assertThat(cb1.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(cb2.getFailureCount()).isZero();
        assertThat(cb2.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldReturnCorrectSize() {
        // given
        registry.getOrCreate("cb1");
        registry.getOrCreate("cb2");

        // when & then
        assertThat(registry.size()).isEqualTo(2);

        registry.remove("cb1");
        assertThat(registry.size()).isEqualTo(1);
    }
}
