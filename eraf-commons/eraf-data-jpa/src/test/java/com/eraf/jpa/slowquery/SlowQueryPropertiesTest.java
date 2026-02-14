package com.eraf.jpa.slowquery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SlowQueryProperties 테스트
 */
class SlowQueryPropertiesTest {

    @Test
    void defaultValues_ShouldBeSet() {
        // given
        SlowQueryProperties properties = new SlowQueryProperties();

        // then
        assertThat(properties.isEnabled()).isFalse();  // 기본 비활성화 (안전)
        assertThat(properties.getThresholdMs()).isEqualTo(1000);  // 1초
        assertThat(properties.getCriticalThresholdMs()).isEqualTo(5000);  // 5초
        assertThat(properties.isLogAsError()).isFalse();
        assertThat(properties.isLogParameters()).isTrue();
        assertThat(properties.isLogStackTrace()).isFalse();
        assertThat(properties.getStackTraceDepth()).isEqualTo(10);
    }

    @Test
    void setValues_ShouldWork() {
        // given
        SlowQueryProperties properties = new SlowQueryProperties();

        // when
        properties.setEnabled(true);
        properties.setThresholdMs(500);
        properties.setCriticalThresholdMs(3000);
        properties.setLogAsError(true);
        properties.setLogParameters(false);
        properties.setLogStackTrace(true);
        properties.setStackTraceDepth(5);

        // then
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getThresholdMs()).isEqualTo(500);
        assertThat(properties.getCriticalThresholdMs()).isEqualTo(3000);
        assertThat(properties.isLogAsError()).isTrue();
        assertThat(properties.isLogParameters()).isFalse();
        assertThat(properties.isLogStackTrace()).isTrue();
        assertThat(properties.getStackTraceDepth()).isEqualTo(5);
    }

    @Test
    void productionSafeDefaults_ShouldBeDisabled() {
        // given
        SlowQueryProperties properties = new SlowQueryProperties();

        // then - 프로덕션 안전성: 기본적으로 비활성화되어야 함
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isLogStackTrace()).isFalse();  // 성능 부담
    }

    @Test
    void developmentSettings_ShouldBeLowThreshold() {
        // given - 개발 환경 설정
        SlowQueryProperties properties = new SlowQueryProperties();

        // when
        properties.setEnabled(true);
        properties.setThresholdMs(100);  // 매우 낮은 임계값
        properties.setLogStackTrace(true);  // 스택 트레이스 활성화

        // then
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getThresholdMs()).isEqualTo(100);
        assertThat(properties.isLogStackTrace()).isTrue();
    }

    @Test
    void criticalThreshold_ShouldBeHigherThanThreshold() {
        // given
        SlowQueryProperties properties = new SlowQueryProperties();

        // then - Critical은 일반 threshold보다 높아야 함
        assertThat(properties.getCriticalThresholdMs())
                .isGreaterThan(properties.getThresholdMs());
    }
}
