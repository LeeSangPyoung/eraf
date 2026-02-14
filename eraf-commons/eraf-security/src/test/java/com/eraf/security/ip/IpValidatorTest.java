package com.eraf.security.ip;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * IP Validator 테스트
 */
class IpValidatorTest {

    @Test
    void isAllowed_WithWhitelistedIp_ShouldReturnTrue() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of("192.168.1.100"),
                List.of()
        );

        // when/then
        assertThat(ipValidator.isAllowed("192.168.1.100", IpAccessControlProperties.AccessPolicy.DENY)).isTrue();
    }

    @Test
    void isAllowed_WithBlacklistedIp_ShouldReturnFalse() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of(),
                List.of("10.0.0.50")
        );

        // when/then
        assertThat(ipValidator.isAllowed("10.0.0.50", IpAccessControlProperties.AccessPolicy.ALLOW)).isFalse();
    }

    @Test
    void isAllowed_WithCidrWhitelist_ShouldMatchRange() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of("192.168.1.0/24"),
                List.of()
        );

        // when/then
        assertThat(ipValidator.isAllowed("192.168.1.1", IpAccessControlProperties.AccessPolicy.DENY)).isTrue();
        assertThat(ipValidator.isAllowed("192.168.1.100", IpAccessControlProperties.AccessPolicy.DENY)).isTrue();
        assertThat(ipValidator.isAllowed("192.168.1.255", IpAccessControlProperties.AccessPolicy.DENY)).isTrue();
        assertThat(ipValidator.isAllowed("192.168.2.1", IpAccessControlProperties.AccessPolicy.DENY)).isFalse();
    }

    @Test
    void isAllowed_WithCidrBlacklist_ShouldMatchRange() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of(),
                List.of("10.0.0.0/8")
        );

        // when/then
        assertThat(ipValidator.isAllowed("10.0.0.1", IpAccessControlProperties.AccessPolicy.ALLOW)).isFalse();
        assertThat(ipValidator.isAllowed("10.255.255.255", IpAccessControlProperties.AccessPolicy.ALLOW)).isFalse();
        assertThat(ipValidator.isAllowed("192.168.1.1", IpAccessControlProperties.AccessPolicy.ALLOW)).isTrue();
    }

    @Test
    void isAllowed_WithDefaultAllowPolicy_ShouldAllowUnlisted() {
        // given
        IpValidator ipValidator = new IpValidator(List.of(), List.of());

        // when/then
        assertThat(ipValidator.isAllowed("192.168.1.1", IpAccessControlProperties.AccessPolicy.ALLOW)).isTrue();
    }

    @Test
    void isAllowed_WithDefaultDenyPolicy_ShouldDenyUnlisted() {
        // given
        IpValidator ipValidator = new IpValidator(List.of(), List.of());

        // when/then
        assertThat(ipValidator.isAllowed("192.168.1.1", IpAccessControlProperties.AccessPolicy.DENY)).isFalse();
    }

    @Test
    void isAllowed_BlacklistOverridesWhitelist() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of("192.168.1.0/24"),
                List.of("192.168.1.100")
        );

        // when/then
        assertThat(ipValidator.isAllowed("192.168.1.50", IpAccessControlProperties.AccessPolicy.DENY)).isTrue();
        assertThat(ipValidator.isAllowed("192.168.1.100", IpAccessControlProperties.AccessPolicy.DENY)).isFalse(); // Blacklist wins
    }

    @Test
    void isWhitelisted_ShouldCheckWhitelist() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of("192.168.1.100", "10.0.0.0/8"),
                List.of()
        );

        // when/then
        assertThat(ipValidator.isWhitelisted("192.168.1.100")).isTrue();
        assertThat(ipValidator.isWhitelisted("10.0.0.1")).isTrue();
        assertThat(ipValidator.isWhitelisted("172.16.0.1")).isFalse();
    }

    @Test
    void isBlacklisted_ShouldCheckBlacklist() {
        // given
        IpValidator ipValidator = new IpValidator(
                List.of(),
                List.of("192.168.1.100", "10.0.0.0/8")
        );

        // when/then
        assertThat(ipValidator.isBlacklisted("192.168.1.100")).isTrue();
        assertThat(ipValidator.isBlacklisted("10.0.0.1")).isTrue();
        assertThat(ipValidator.isBlacklisted("172.16.0.1")).isFalse();
    }

    @Test
    void isWhitelisted_WithEmptyWhitelist_ShouldReturnFalse() {
        // given
        IpValidator ipValidator = new IpValidator(List.of(), List.of());

        // when/then
        assertThat(ipValidator.isWhitelisted("192.168.1.100")).isFalse();
    }

    @Test
    void isBlacklisted_WithEmptyBlacklist_ShouldReturnFalse() {
        // given
        IpValidator ipValidator = new IpValidator(List.of(), List.of());

        // when/then
        assertThat(ipValidator.isBlacklisted("192.168.1.100")).isFalse();
    }
}
