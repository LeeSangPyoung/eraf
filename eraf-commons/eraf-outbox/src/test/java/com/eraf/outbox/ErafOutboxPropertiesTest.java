package com.eraf.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErafOutboxProperties 테스트")
class ErafOutboxPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void defaultValues_ShouldBeCorrect() {
        // given
        ErafOutboxProperties props = new ErafOutboxProperties();

        // then
        assertThat(props.isEnabled()).isTrue();
        assertThat(props.getBatchSize()).isEqualTo(100);
        assertThat(props.getMaxRetries()).isEqualTo(3);
        assertThat(props.getPollInterval()).isEqualTo(5000);
        assertThat(props.getCleanupDays()).isEqualTo(7);
        assertThat(props.getCleanupCron()).isEqualTo("0 0 3 * * ?");
    }

    @Test
    @DisplayName("값 변경 확인")
    void setters_ShouldUpdateValues() {
        // given
        ErafOutboxProperties props = new ErafOutboxProperties();

        // when
        props.setEnabled(false);
        props.setBatchSize(50);
        props.setMaxRetries(5);
        props.setPollInterval(10000);
        props.setCleanupDays(14);
        props.setCleanupCron("0 0 4 * * ?");

        // then
        assertThat(props.isEnabled()).isFalse();
        assertThat(props.getBatchSize()).isEqualTo(50);
        assertThat(props.getMaxRetries()).isEqualTo(5);
        assertThat(props.getPollInterval()).isEqualTo(10000);
        assertThat(props.getCleanupDays()).isEqualTo(14);
        assertThat(props.getCleanupCron()).isEqualTo("0 0 4 * * ?");
    }
}
