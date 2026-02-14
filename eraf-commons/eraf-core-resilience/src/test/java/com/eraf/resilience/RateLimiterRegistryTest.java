package com.eraf.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Rate Limiter Registry Unit Tests
 */
class RateLimiterRegistryTest {

    private RateLimiter.Registry registry;

    @BeforeEach
    void setUp() {
        registry = new RateLimiter.Registry(100.0, 150);
    }

    @Test
    void shouldCreateRateLimiterWithDefaultSettings() {
        // when
        RateLimiter limiter = registry.getOrCreate("test");

        // then
        assertThat(limiter).isNotNull();
        assertThat(limiter.getName()).isEqualTo("test");
        assertThat(limiter.getPermitsPerSecond()).isEqualTo(100.0);
    }

    @Test
    void shouldReturnSameInstanceForSameName() {
        // when
        RateLimiter limiter1 = registry.getOrCreate("test");
        RateLimiter limiter2 = registry.getOrCreate("test");

        // then
        assertThat(limiter1).isSameAs(limiter2);
    }

    @Test
    void shouldCreateRateLimiterWithCustomSettings() {
        // when
        RateLimiter limiter = registry.getOrCreate("custom", 50.0, 75);

        // then
        assertThat(limiter.getName()).isEqualTo("custom");
        assertThat(limiter.getPermitsPerSecond()).isEqualTo(50.0);
    }

    @Test
    void shouldGetExistingRateLimiter() {
        // given
        registry.getOrCreate("test");

        // when
        RateLimiter limiter = registry.get("test");

        // then
        assertThat(limiter).isNotNull();
        assertThat(limiter.getName()).isEqualTo("test");
    }

    @Test
    void shouldReturnNullForNonExistentRateLimiter() {
        // when
        RateLimiter limiter = registry.get("nonexistent");

        // then
        assertThat(limiter).isNull();
    }

    @Test
    void shouldRemoveRateLimiter() {
        // given
        registry.getOrCreate("test");
        assertThat(registry.get("test")).isNotNull();

        // when
        registry.remove("test");

        // then
        assertThat(registry.get("test")).isNull();
    }
}
