package com.eraf.core.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP 주소 유틸리티
 * 클라이언트 IP 추출, IP 검증, IP 범위 체크 등
 */
public final class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
            "^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
            "^([0-9a-fA-F]{1,4}:){1,7}:$"
    );

    private IpUtils() {
    }

    // ===== 클라이언트 IP 추출 =====

    /**
     * HttpServletRequest에서 실제 클라이언트 IP 추출
     * 프록시, 로드밸런서를 거쳐도 실제 IP를 찾음
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For: client, proxy1, proxy2 형식일 수 있음
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            }
            return ip.trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    /**
     * IP가 유효한지 확인 (null, empty, unknown 체크)
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }

    // ===== IP 검증 =====

    /**
     * IPv4 형식인지 확인
     */
    public static boolean isIpv4(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip).matches();
    }

    /**
     * IPv6 형식인지 확인
     */
    public static boolean isIpv6(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return IPV6_PATTERN.matcher(ip).matches();
    }

    /**
     * 유효한 IP 주소인지 확인 (IPv4 또는 IPv6)
     */
    public static boolean isValidIpAddress(String ip) {
        return isIpv4(ip) || isIpv6(ip);
    }

    // ===== IPv4 변환 =====

    /**
     * IPv4 문자열을 long으로 변환
     * 예: "192.168.1.1" -> 3232235777
     */
    public static long ipToLong(String ip) {
        if (!isIpv4(ip)) {
            throw new IllegalArgumentException("유효하지 않은 IPv4 주소: " + ip);
        }

        String[] octets = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Long.parseLong(octets[i]) << (24 - (8 * i)));
        }
        return result;
    }

    /**
     * long을 IPv4 문자열로 변환
     * 예: 3232235777 -> "192.168.1.1"
     */
    public static String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }

    // ===== IP 범위 체크 =====

    /**
     * IP가 특정 범위 내에 있는지 확인
     * 예: isInRange("192.168.1.100", "192.168.1.1", "192.168.1.255")
     */
    public static boolean isInRange(String ip, String startIp, String endIp) {
        if (!isIpv4(ip) || !isIpv4(startIp) || !isIpv4(endIp)) {
            return false;
        }
        long ipLong = ipToLong(ip);
        long startLong = ipToLong(startIp);
        long endLong = ipToLong(endIp);
        return ipLong >= startLong && ipLong <= endLong;
    }

    /**
     * IP가 CIDR 범위 내에 있는지 확인
     * 예: isInCidr("192.168.1.100", "192.168.1.0/24")
     */
    public static boolean isInCidr(String ip, String cidr) {
        if (!isIpv4(ip)) {
            return false;
        }

        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }

        String networkIp = parts[0];
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (prefixLength < 0 || prefixLength > 32) {
            return false;
        }

        long ipLong = ipToLong(ip);
        long networkLong = ipToLong(networkIp);
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

        return (ipLong & mask) == (networkLong & mask);
    }

    // ===== 사설 IP 체크 =====

    /**
     * 사설 IP인지 확인
     * 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
     */
    public static boolean isPrivateIp(String ip) {
        if (!isIpv4(ip)) {
            return false;
        }

        // 10.0.0.0/8
        if (isInCidr(ip, "10.0.0.0/8")) {
            return true;
        }

        // 172.16.0.0/12
        if (isInCidr(ip, "172.16.0.0/12")) {
            return true;
        }

        // 192.168.0.0/16
        if (isInCidr(ip, "192.168.0.0/16")) {
            return true;
        }

        return false;
    }

    /**
     * 공인 IP인지 확인
     */
    public static boolean isPublicIp(String ip) {
        if (!isValidIpAddress(ip)) {
            return false;
        }
        return !isPrivateIp(ip) && !isLoopbackIp(ip) && !isLinkLocalIp(ip);
    }

    /**
     * 루프백 IP인지 확인 (127.0.0.0/8)
     */
    public static boolean isLoopbackIp(String ip) {
        if (!isIpv4(ip)) {
            return false;
        }
        return isInCidr(ip, "127.0.0.0/8");
    }

    /**
     * 링크 로컬 IP인지 확인 (169.254.0.0/16)
     */
    public static boolean isLinkLocalIp(String ip) {
        if (!isIpv4(ip)) {
            return false;
        }
        return isInCidr(ip, "169.254.0.0/16");
    }

    // ===== IP 마스킹 =====

    /**
     * IP 주소를 마스킹 (개인정보 보호)
     * 예: "192.168.1.100" -> "192.168.***.***"
     */
    public static String maskIp(String ip) {
        if (!isIpv4(ip)) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        return parts[0] + "." + parts[1] + ".***." + "***";
    }

    /**
     * IP 주소를 마스킹 (마지막 옥텟만)
     * 예: "192.168.1.100" -> "192.168.1.***"
     */
    public static String maskLastOctet(String ip) {
        if (!isIpv4(ip)) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        return parts[0] + "." + parts[1] + "." + parts[2] + ".***";
    }

    // ===== 호스트명 변환 =====

    /**
     * IP 주소를 호스트명으로 변환
     */
    public static String ipToHostname(String ip) {
        if (!isValidIpAddress(ip)) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 호스트명을 IP 주소로 변환
     */
    public static String hostnameToIp(String hostname) {
        if (hostname == null || hostname.isEmpty()) {
            return null;
        }
        try {
            InetAddress address = InetAddress.getByName(hostname);
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    // ===== 로컬 IP 정보 =====

    /**
     * 로컬 호스트의 IP 주소 반환
     */
    public static String getLocalIp() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 로컬 호스트명 반환
     */
    public static String getLocalHostname() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    // ===== 네트워크 주소 계산 =====

    /**
     * CIDR에서 네트워크 주소 계산
     * 예: "192.168.1.100/24" -> "192.168.1.0"
     */
    public static String getNetworkAddress(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("유효하지 않은 CIDR 형식: " + cidr);
        }

        String ip = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        long ipLong = ipToLong(ip);
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        long networkLong = ipLong & mask;

        return longToIp(networkLong);
    }

    /**
     * CIDR에서 브로드캐스트 주소 계산
     * 예: "192.168.1.0/24" -> "192.168.1.255"
     */
    public static String getBroadcastAddress(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("유효하지 않은 CIDR 형식: " + cidr);
        }

        String ip = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        long ipLong = ipToLong(ip);
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        long networkLong = ipLong & mask;
        long broadcastLong = networkLong | (~mask & 0xFFFFFFFFL);

        return longToIp(broadcastLong);
    }

    /**
     * CIDR에서 사용 가능한 IP 개수 계산
     * 예: "/24" -> 254 (네트워크 주소와 브로드캐스트 제외)
     */
    public static int getAvailableIpCount(int prefixLength) {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Prefix length는 0-32 사이여야 합니다");
        }
        if (prefixLength == 32) {
            return 1;
        }
        if (prefixLength == 31) {
            return 2;
        }
        return (int) Math.pow(2, 32 - prefixLength) - 2;
    }

    // ===== 서브넷 마스크 =====

    /**
     * Prefix length를 서브넷 마스크로 변환
     * 예: 24 -> "255.255.255.0"
     */
    public static String prefixLengthToSubnetMask(int prefixLength) {
        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Prefix length는 0-32 사이여야 합니다");
        }
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        return longToIp(mask);
    }

    /**
     * 서브넷 마스크를 Prefix length로 변환
     * 예: "255.255.255.0" -> 24
     */
    public static int subnetMaskToPrefixLength(String subnetMask) {
        if (!isIpv4(subnetMask)) {
            throw new IllegalArgumentException("유효하지 않은 서브넷 마스크: " + subnetMask);
        }
        long mask = ipToLong(subnetMask);
        return Long.bitCount(mask);
    }

    // ===== 유틸리티 =====

    /**
     * IPv4 주소를 정규화 (앞의 0 제거)
     * 예: "192.168.001.100" -> "192.168.1.100"
     */
    public static String normalizeIpv4(String ip) {
        if (!isIpv4(ip)) {
            return ip;
        }
        String[] parts = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(".");
            }
            sb.append(Integer.parseInt(parts[i]));
        }
        return sb.toString();
    }

    /**
     * 두 IP 주소 비교 (같으면 0, ip1 < ip2면 음수, ip1 > ip2면 양수)
     */
    public static int compare(String ip1, String ip2) {
        if (!isIpv4(ip1) || !isIpv4(ip2)) {
            throw new IllegalArgumentException("유효하지 않은 IPv4 주소");
        }
        long long1 = ipToLong(ip1);
        long long2 = ipToLong(ip2);
        return Long.compare(long1, long2);
    }
}
