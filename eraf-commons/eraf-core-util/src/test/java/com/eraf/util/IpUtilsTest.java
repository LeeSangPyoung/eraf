package com.eraf.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IpUtils - IP 주소 유틸리티")
class IpUtilsTest {

    @Nested
    @DisplayName("IPv4 검증")
    class Ipv4Validation {

        @Test
        @DisplayName("유효한 IPv4 주소")
        void validIpv4() {
            assertTrue(IpUtils.isIpv4("192.168.1.1"));
            assertTrue(IpUtils.isIpv4("0.0.0.0"));
            assertTrue(IpUtils.isIpv4("255.255.255.255"));
            assertTrue(IpUtils.isIpv4("10.0.0.1"));
        }

        @Test
        @DisplayName("잘못된 IPv4 주소")
        void invalidIpv4() {
            assertFalse(IpUtils.isIpv4(null));
            assertFalse(IpUtils.isIpv4(""));
            assertFalse(IpUtils.isIpv4("256.1.1.1"));
            assertFalse(IpUtils.isIpv4("1.2.3"));
            assertFalse(IpUtils.isIpv4("abc.def.ghi.jkl"));
            assertFalse(IpUtils.isIpv4("1.2.3.4.5"));
        }
    }

    @Nested
    @DisplayName("IPv6 검증")
    class Ipv6Validation {

        @Test
        @DisplayName("유효한 IPv6 주소")
        void validIpv6() {
            assertTrue(IpUtils.isIpv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
            assertTrue(IpUtils.isIpv6("::1"));
        }

        @Test
        @DisplayName("잘못된 IPv6 주소")
        void invalidIpv6() {
            assertFalse(IpUtils.isIpv6(null));
            assertFalse(IpUtils.isIpv6(""));
            assertFalse(IpUtils.isIpv6("192.168.1.1"));
        }
    }

    @Nested
    @DisplayName("IP 주소 유효성")
    class IpAddressValidity {

        @Test
        @DisplayName("isValidIpAddress - IPv4와 IPv6 모두")
        void isValidIpAddress() {
            assertTrue(IpUtils.isValidIpAddress("192.168.1.1"));
            assertTrue(IpUtils.isValidIpAddress("::1"));
            assertFalse(IpUtils.isValidIpAddress("invalid"));
            assertFalse(IpUtils.isValidIpAddress(null));
        }
    }

    @Nested
    @DisplayName("IPv4 변환")
    class Ipv4Conversion {

        @Test
        @DisplayName("ipToLong 변환")
        void ipToLong() {
            assertEquals(3232235777L, IpUtils.ipToLong("192.168.1.1"));
            assertEquals(0L, IpUtils.ipToLong("0.0.0.0"));
            assertEquals(4294967295L, IpUtils.ipToLong("255.255.255.255"));
        }

        @Test
        @DisplayName("longToIp 변환")
        void longToIp() {
            assertEquals("192.168.1.1", IpUtils.longToIp(3232235777L));
            assertEquals("0.0.0.0", IpUtils.longToIp(0L));
            assertEquals("255.255.255.255", IpUtils.longToIp(4294967295L));
        }

        @Test
        @DisplayName("ipToLong - 잘못된 IP")
        void ipToLongInvalid() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.ipToLong("invalid"));
        }

        @Test
        @DisplayName("양방향 변환 일관성")
        void roundTrip() {
            String ip = "172.16.0.1";
            assertEquals(ip, IpUtils.longToIp(IpUtils.ipToLong(ip)));
        }
    }

    @Nested
    @DisplayName("IP 범위 체크")
    class IpRangeCheck {

        @Test
        @DisplayName("isInRange - 범위 내")
        void isInRange() {
            assertTrue(IpUtils.isInRange("192.168.1.100", "192.168.1.1", "192.168.1.255"));
            assertTrue(IpUtils.isInRange("192.168.1.1", "192.168.1.1", "192.168.1.255"));
            assertTrue(IpUtils.isInRange("192.168.1.255", "192.168.1.1", "192.168.1.255"));
        }

        @Test
        @DisplayName("isInRange - 범위 밖")
        void isInRangeOutside() {
            assertFalse(IpUtils.isInRange("192.168.2.1", "192.168.1.1", "192.168.1.255"));
            assertFalse(IpUtils.isInRange("invalid", "192.168.1.1", "192.168.1.255"));
        }

        @Test
        @DisplayName("isInCidr - CIDR 범위")
        void isInCidr() {
            assertTrue(IpUtils.isInCidr("192.168.1.100", "192.168.1.0/24"));
            assertTrue(IpUtils.isInCidr("10.0.0.1", "10.0.0.0/8"));
            assertFalse(IpUtils.isInCidr("192.168.2.1", "192.168.1.0/24"));
        }

        @Test
        @DisplayName("isInCidr - 잘못된 CIDR")
        void isInCidrInvalid() {
            assertFalse(IpUtils.isInCidr("192.168.1.1", "invalid"));
            assertFalse(IpUtils.isInCidr("192.168.1.1", "192.168.1.0"));
            assertFalse(IpUtils.isInCidr("192.168.1.1", "192.168.1.0/abc"));
            assertFalse(IpUtils.isInCidr("192.168.1.1", "192.168.1.0/33"));
            assertFalse(IpUtils.isInCidr("invalid", "192.168.1.0/24"));
        }
    }

    @Nested
    @DisplayName("사설/공인 IP")
    class PrivatePublicIp {

        @Test
        @DisplayName("사설 IP 확인")
        void isPrivateIp() {
            assertTrue(IpUtils.isPrivateIp("10.0.0.1"));
            assertTrue(IpUtils.isPrivateIp("10.255.255.255"));
            assertTrue(IpUtils.isPrivateIp("172.16.0.1"));
            assertTrue(IpUtils.isPrivateIp("172.31.255.255"));
            assertTrue(IpUtils.isPrivateIp("192.168.0.1"));
            assertTrue(IpUtils.isPrivateIp("192.168.255.255"));
        }

        @Test
        @DisplayName("공인 IP 확인")
        void isPublicIp() {
            assertTrue(IpUtils.isPublicIp("8.8.8.8"));
            assertTrue(IpUtils.isPublicIp("1.1.1.1"));
            assertFalse(IpUtils.isPublicIp("192.168.1.1"));
            assertFalse(IpUtils.isPublicIp("127.0.0.1"));
            assertFalse(IpUtils.isPublicIp("169.254.1.1"));
            assertFalse(IpUtils.isPublicIp("invalid"));
        }

        @Test
        @DisplayName("루프백 IP 확인")
        void isLoopbackIp() {
            assertTrue(IpUtils.isLoopbackIp("127.0.0.1"));
            assertTrue(IpUtils.isLoopbackIp("127.255.255.255"));
            assertFalse(IpUtils.isLoopbackIp("192.168.1.1"));
            assertFalse(IpUtils.isLoopbackIp("invalid"));
        }

        @Test
        @DisplayName("링크 로컬 IP 확인")
        void isLinkLocalIp() {
            assertTrue(IpUtils.isLinkLocalIp("169.254.0.1"));
            assertTrue(IpUtils.isLinkLocalIp("169.254.255.255"));
            assertFalse(IpUtils.isLinkLocalIp("192.168.1.1"));
            assertFalse(IpUtils.isLinkLocalIp("invalid"));
        }

        @Test
        @DisplayName("사설IP 아닌 경우")
        void isNotPrivateIp() {
            assertFalse(IpUtils.isPrivateIp("8.8.8.8"));
            assertFalse(IpUtils.isPrivateIp("invalid"));
        }
    }

    @Nested
    @DisplayName("IP 마스킹")
    class IpMasking {

        @Test
        @DisplayName("maskIp - 3,4번째 옥텟 마스킹")
        void maskIp() {
            assertEquals("192.168.***.***", IpUtils.maskIp("192.168.1.100"));
        }

        @Test
        @DisplayName("maskLastOctet - 마지막 옥텟만 마스킹")
        void maskLastOctet() {
            assertEquals("192.168.1.***", IpUtils.maskLastOctet("192.168.1.100"));
        }

        @Test
        @DisplayName("잘못된 IP 마스킹 - 원본 반환")
        void maskInvalidIp() {
            assertEquals("invalid", IpUtils.maskIp("invalid"));
            assertEquals("invalid", IpUtils.maskLastOctet("invalid"));
        }
    }

    @Nested
    @DisplayName("네트워크 주소 계산")
    class NetworkCalculation {

        @Test
        @DisplayName("getNetworkAddress")
        void getNetworkAddress() {
            assertEquals("192.168.1.0", IpUtils.getNetworkAddress("192.168.1.100/24"));
            assertEquals("10.0.0.0", IpUtils.getNetworkAddress("10.1.2.3/8"));
        }

        @Test
        @DisplayName("getBroadcastAddress")
        void getBroadcastAddress() {
            assertEquals("192.168.1.255", IpUtils.getBroadcastAddress("192.168.1.0/24"));
            assertEquals("10.255.255.255", IpUtils.getBroadcastAddress("10.0.0.0/8"));
        }

        @Test
        @DisplayName("getAvailableIpCount")
        void getAvailableIpCount() {
            assertEquals(254, IpUtils.getAvailableIpCount(24));
            assertEquals(65534, IpUtils.getAvailableIpCount(16));
            assertEquals(1, IpUtils.getAvailableIpCount(32));
            assertEquals(2, IpUtils.getAvailableIpCount(31));
        }

        @Test
        @DisplayName("getAvailableIpCount - 잘못된 prefix")
        void getAvailableIpCountInvalid() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.getAvailableIpCount(-1));
            assertThrows(IllegalArgumentException.class, () -> IpUtils.getAvailableIpCount(33));
        }

        @Test
        @DisplayName("getNetworkAddress - 잘못된 CIDR")
        void getNetworkAddressInvalid() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.getNetworkAddress("invalid"));
        }
    }

    @Nested
    @DisplayName("서브넷 마스크")
    class SubnetMask {

        @Test
        @DisplayName("prefixLengthToSubnetMask")
        void prefixLengthToSubnetMask() {
            assertEquals("255.255.255.0", IpUtils.prefixLengthToSubnetMask(24));
            assertEquals("255.255.0.0", IpUtils.prefixLengthToSubnetMask(16));
            assertEquals("255.0.0.0", IpUtils.prefixLengthToSubnetMask(8));
            assertEquals("255.255.255.255", IpUtils.prefixLengthToSubnetMask(32));
        }

        @Test
        @DisplayName("subnetMaskToPrefixLength")
        void subnetMaskToPrefixLength() {
            assertEquals(24, IpUtils.subnetMaskToPrefixLength("255.255.255.0"));
            assertEquals(16, IpUtils.subnetMaskToPrefixLength("255.255.0.0"));
            assertEquals(8, IpUtils.subnetMaskToPrefixLength("255.0.0.0"));
        }

        @Test
        @DisplayName("잘못된 prefix length")
        void invalidPrefixLength() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.prefixLengthToSubnetMask(-1));
            assertThrows(IllegalArgumentException.class, () -> IpUtils.prefixLengthToSubnetMask(33));
        }

        @Test
        @DisplayName("잘못된 서브넷 마스크")
        void invalidSubnetMask() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.subnetMaskToPrefixLength("invalid"));
        }
    }

    @Nested
    @DisplayName("유틸리티")
    class Utility {

        @Test
        @DisplayName("normalizeIpv4")
        void normalizeIpv4() {
            assertEquals("192.168.1.100", IpUtils.normalizeIpv4("192.168.1.100"));
            // 잘못된 IP는 원본 반환
            assertEquals("invalid", IpUtils.normalizeIpv4("invalid"));
        }

        @Test
        @DisplayName("compare")
        void compare() {
            assertTrue(IpUtils.compare("192.168.1.2", "192.168.1.1") > 0);
            assertTrue(IpUtils.compare("192.168.1.1", "192.168.1.2") < 0);
            assertEquals(0, IpUtils.compare("192.168.1.1", "192.168.1.1"));
        }

        @Test
        @DisplayName("compare - 잘못된 IP")
        void compareInvalid() {
            assertThrows(IllegalArgumentException.class, () -> IpUtils.compare("invalid", "192.168.1.1"));
        }

        @Test
        @DisplayName("getLocalIp - null 아님")
        void getLocalIp() {
            String localIp = IpUtils.getLocalIp();
            assertNotNull(localIp);
        }

        @Test
        @DisplayName("getLocalHostname - null 아님")
        void getLocalHostname() {
            String hostname = IpUtils.getLocalHostname();
            assertNotNull(hostname);
        }
    }
}
