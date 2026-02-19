package com.eraf.featureflag.evaluator;

import com.eraf.featureflag.feature.FeatureFlagEntity;
import com.eraf.featureflag.feature.evaluator.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== Simple Evaluator =====

    @Nested
    @DisplayName("SimpleEvaluator")
    class SimpleEvaluatorTest {

        private SimpleEvaluator evaluator;

        @BeforeEach
        void setUp() {
            evaluator = new SimpleEvaluator();
        }

        @Test
        @DisplayName("SIMPLE 타입 지원")
        void testSupportsSimple() {
            assertTrue(evaluator.supports(FeatureFlagEntity.FlagType.SIMPLE));
            assertFalse(evaluator.supports(FeatureFlagEntity.FlagType.PERCENTAGE));
        }

        @Test
        @DisplayName("활성 플래그 → true")
        void testEnabledFlag() {
            FeatureFlagEntity flag = createFlag("test", true, FeatureFlagEntity.FlagType.SIMPLE, null);
            assertTrue(evaluator.evaluate(flag, null));
        }

        @Test
        @DisplayName("비활성 플래그 → false")
        void testDisabledFlag() {
            FeatureFlagEntity flag = createFlag("test", false, FeatureFlagEntity.FlagType.SIMPLE, null);
            assertFalse(evaluator.evaluate(flag, null));
        }
    }

    // ===== User-Based Evaluator =====

    @Nested
    @DisplayName("UserBasedEvaluator")
    class UserBasedEvaluatorTest {

        private UserBasedEvaluator evaluator;

        @BeforeEach
        void setUp() {
            evaluator = new UserBasedEvaluator(objectMapper);
        }

        @Test
        @DisplayName("USER_BASED 타입 지원")
        void testSupports() {
            assertTrue(evaluator.supports(FeatureFlagEntity.FlagType.USER_BASED));
            assertFalse(evaluator.supports(FeatureFlagEntity.FlagType.SIMPLE));
        }

        @Test
        @DisplayName("허용된 userId 매칭")
        void testUserIdMatch() {
            String rules = "{\"userIds\": [\"user1\", \"user2\"]}";
            FeatureFlagEntity flag = createFlag("beta", true, FeatureFlagEntity.FlagType.USER_BASED, rules);

            assertTrue(evaluator.evaluate(flag, Map.of("userId", "user1")));
            assertTrue(evaluator.evaluate(flag, Map.of("userId", "user2")));
            assertFalse(evaluator.evaluate(flag, Map.of("userId", "user3")));
        }

        @Test
        @DisplayName("허용된 userGroup 매칭")
        void testUserGroupMatch() {
            String rules = "{\"userGroups\": [\"beta-testers\"]}";
            FeatureFlagEntity flag = createFlag("beta", true, FeatureFlagEntity.FlagType.USER_BASED, rules);

            Map<String, Object> context = Map.of(
                    "userId", "user1",
                    "userGroups", List.of("beta-testers", "premium"));
            assertTrue(evaluator.evaluate(flag, context));
        }

        @Test
        @DisplayName("매칭 안되는 사용자 → false")
        void testNoMatch() {
            String rules = "{\"userIds\": [\"admin\"]}";
            FeatureFlagEntity flag = createFlag("admin-only", true, FeatureFlagEntity.FlagType.USER_BASED, rules);

            assertFalse(evaluator.evaluate(flag, Map.of("userId", "regular-user")));
        }

        @Test
        @DisplayName("컨텍스트 없으면 false")
        void testNoContext() {
            String rules = "{\"userIds\": [\"user1\"]}";
            FeatureFlagEntity flag = createFlag("beta", true, FeatureFlagEntity.FlagType.USER_BASED, rules);

            assertFalse(evaluator.evaluate(flag, null));
            assertFalse(evaluator.evaluate(flag, Map.of()));
        }

        @Test
        @DisplayName("비활성 플래그 → false")
        void testDisabledFlag() {
            String rules = "{\"userIds\": [\"user1\"]}";
            FeatureFlagEntity flag = createFlag("beta", false, FeatureFlagEntity.FlagType.USER_BASED, rules);

            assertFalse(evaluator.evaluate(flag, Map.of("userId", "user1")));
        }
    }

    // ===== Percentage Evaluator =====

    @Nested
    @DisplayName("PercentageEvaluator")
    class PercentageEvaluatorTest {

        private PercentageEvaluator evaluator;

        @BeforeEach
        void setUp() {
            evaluator = new PercentageEvaluator(objectMapper);
        }

        @Test
        @DisplayName("PERCENTAGE 타입 지원")
        void testSupports() {
            assertTrue(evaluator.supports(FeatureFlagEntity.FlagType.PERCENTAGE));
            assertFalse(evaluator.supports(FeatureFlagEntity.FlagType.SIMPLE));
        }

        @Test
        @DisplayName("100% → 항상 true")
        void testFullRollout() {
            String rules = "{\"percentage\": 100}";
            FeatureFlagEntity flag = createFlag("full-rollout", true, FeatureFlagEntity.FlagType.PERCENTAGE, rules);

            assertTrue(evaluator.evaluate(flag, Map.of("userId", "user1")));
            assertTrue(evaluator.evaluate(flag, Map.of("userId", "user2")));
        }

        @Test
        @DisplayName("0% → 항상 false")
        void testNoRollout() {
            String rules = "{\"percentage\": 0}";
            FeatureFlagEntity flag = createFlag("no-rollout", true, FeatureFlagEntity.FlagType.PERCENTAGE, rules);

            assertFalse(evaluator.evaluate(flag, Map.of("userId", "user1")));
        }

        @Test
        @DisplayName("일관적 해싱 - 같은 사용자 같은 결과")
        void testConsistentHash() {
            String rules = "{\"percentage\": 50}";
            FeatureFlagEntity flag = createFlag("half-rollout", true, FeatureFlagEntity.FlagType.PERCENTAGE, rules);

            boolean result1 = evaluator.evaluate(flag, Map.of("userId", "consistent-user"));
            boolean result2 = evaluator.evaluate(flag, Map.of("userId", "consistent-user"));
            boolean result3 = evaluator.evaluate(flag, Map.of("userId", "consistent-user"));

            assertEquals(result1, result2);
            assertEquals(result2, result3);
        }

        @Test
        @DisplayName("비활성 플래그 → false")
        void testDisabledFlag() {
            String rules = "{\"percentage\": 100}";
            FeatureFlagEntity flag = createFlag("disabled", false, FeatureFlagEntity.FlagType.PERCENTAGE, rules);

            assertFalse(evaluator.evaluate(flag, Map.of("userId", "user1")));
        }
    }

    // ===== Time Window Evaluator =====

    @Nested
    @DisplayName("TimeWindowEvaluator")
    class TimeWindowEvaluatorTest {

        private TimeWindowEvaluator evaluator;

        @BeforeEach
        void setUp() {
            evaluator = new TimeWindowEvaluator(objectMapper);
        }

        @Test
        @DisplayName("TIME_WINDOW 타입 지원")
        void testSupports() {
            assertTrue(evaluator.supports(FeatureFlagEntity.FlagType.TIME_WINDOW));
            assertFalse(evaluator.supports(FeatureFlagEntity.FlagType.SIMPLE));
        }

        @Test
        @DisplayName("현재 시간이 윈도우 안이면 true")
        void testWithinWindow() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            String rules = String.format(
                    "{\"timeWindows\": [{\"start\": \"%s\", \"end\": \"%s\"}]}", start, end);

            FeatureFlagEntity flag = createFlag("timed", true, FeatureFlagEntity.FlagType.TIME_WINDOW, rules);
            assertTrue(evaluator.evaluate(flag, null));
        }

        @Test
        @DisplayName("현재 시간이 윈도우 밖이면 false")
        void testOutsideWindow() {
            Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(2, ChronoUnit.HOURS);
            String rules = String.format(
                    "{\"timeWindows\": [{\"start\": \"%s\", \"end\": \"%s\"}]}", start, end);

            FeatureFlagEntity flag = createFlag("future", true, FeatureFlagEntity.FlagType.TIME_WINDOW, rules);
            assertFalse(evaluator.evaluate(flag, null));
        }

        @Test
        @DisplayName("과거 윈도우 → false")
        void testPastWindow() {
            Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
            Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
            String rules = String.format(
                    "{\"timeWindows\": [{\"start\": \"%s\", \"end\": \"%s\"}]}", start, end);

            FeatureFlagEntity flag = createFlag("past", true, FeatureFlagEntity.FlagType.TIME_WINDOW, rules);
            assertFalse(evaluator.evaluate(flag, null));
        }

        @Test
        @DisplayName("비활성 플래그 → false")
        void testDisabledFlag() {
            Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().plus(1, ChronoUnit.HOURS);
            String rules = String.format(
                    "{\"timeWindows\": [{\"start\": \"%s\", \"end\": \"%s\"}]}", start, end);

            FeatureFlagEntity flag = createFlag("disabled", false, FeatureFlagEntity.FlagType.TIME_WINDOW, rules);
            assertFalse(evaluator.evaluate(flag, null));
        }

        @Test
        @DisplayName("빈 타겟팅 규칙 → false")
        void testEmptyWindows() {
            String rules = "{\"timeWindows\": []}";
            FeatureFlagEntity flag = createFlag("empty", true, FeatureFlagEntity.FlagType.TIME_WINDOW, rules);
            assertFalse(evaluator.evaluate(flag, null));
        }
    }

    // ===== Helper =====

    private FeatureFlagEntity createFlag(String key, boolean enabled,
                                          FeatureFlagEntity.FlagType type, String targetingRules) {
        FeatureFlagEntity entity = new FeatureFlagEntity();
        entity.setId(1L);
        entity.setFlagKey(key);
        entity.setName(key);
        entity.setEnabled(enabled);
        entity.setFlagType(type);
        entity.setTargetingRules(targetingRules);
        entity.setCheckCount(0L);
        return entity;
    }
}
