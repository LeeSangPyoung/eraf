package com.eraf.core.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * 시스템 정보 유틸리티
 * OS, JVM, 메모리, CPU 등 시스템 정보 조회
 */
public final class SystemUtils {

    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    private SystemUtils() {
    }

    // ===== OS 정보 =====

    /**
     * OS 이름 반환
     * 예: "Windows 11", "Linux", "Mac OS X"
     */
    public static String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * OS 버전 반환
     */
    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * OS 아키텍처 반환
     * 예: "amd64", "x86", "aarch64"
     */
    public static String getOsArch() {
        return System.getProperty("os.arch");
    }

    /**
     * Windows OS인지 확인
     */
    public static boolean isWindows() {
        return getOsName().toLowerCase().contains("win");
    }

    /**
     * Linux OS인지 확인
     */
    public static boolean isLinux() {
        return getOsName().toLowerCase().contains("linux");
    }

    /**
     * Mac OS인지 확인
     */
    public static boolean isMac() {
        String os = getOsName().toLowerCase();
        return os.contains("mac") || os.contains("darwin");
    }

    /**
     * Unix 계열 OS인지 확인 (Linux, Mac, Unix)
     */
    public static boolean isUnix() {
        String os = getOsName().toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix") ||
               os.contains("mac") || os.contains("darwin");
    }

    // ===== Java/JVM 정보 =====

    /**
     * Java 버전 반환
     * 예: "17.0.1"
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Java 주요 버전 반환
     * 예: 17
     */
    public static int getJavaMajorVersion() {
        String version = getJavaVersion();
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dotIndex = version.indexOf('.');
            if (dotIndex != -1) {
                version = version.substring(0, dotIndex);
            }
        }
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Java 홈 디렉토리 반환
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    /**
     * Java 벤더 반환
     * 예: "Oracle Corporation", "Eclipse Adoptium"
     */
    public static String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

    /**
     * JVM 이름 반환
     * 예: "Java HotSpot(TM) 64-Bit Server VM"
     */
    public static String getJvmName() {
        return System.getProperty("java.vm.name");
    }

    /**
     * JVM 벤더 반환
     */
    public static String getJvmVendor() {
        return System.getProperty("java.vm.vendor");
    }

    /**
     * JVM 버전 반환
     */
    public static String getJvmVersion() {
        return System.getProperty("java.vm.version");
    }

    /**
     * JVM 시작 시간 (밀리초)
     */
    public static long getJvmStartTime() {
        return RUNTIME_MX_BEAN.getStartTime();
    }

    /**
     * JVM 실행 시간 (밀리초)
     */
    public static long getJvmUptime() {
        return RUNTIME_MX_BEAN.getUptime();
    }

    // ===== 메모리 정보 =====

    /**
     * 최대 메모리 (바이트)
     * JVM이 사용할 수 있는 최대 메모리 (-Xmx)
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * 최대 메모리 (MB)
     */
    public static long getMaxMemoryMb() {
        return getMaxMemory() / (1024 * 1024);
    }

    /**
     * 전체 메모리 (바이트)
     * JVM이 현재 할당한 메모리
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * 전체 메모리 (MB)
     */
    public static long getTotalMemoryMb() {
        return getTotalMemory() / (1024 * 1024);
    }

    /**
     * 여유 메모리 (바이트)
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * 여유 메모리 (MB)
     */
    public static long getFreeMemoryMb() {
        return getFreeMemory() / (1024 * 1024);
    }

    /**
     * 사용 중인 메모리 (바이트)
     */
    public static long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }

    /**
     * 사용 중인 메모리 (MB)
     */
    public static long getUsedMemoryMb() {
        return getUsedMemory() / (1024 * 1024);
    }

    /**
     * 메모리 사용률 (%)
     */
    public static double getMemoryUsagePercent() {
        long total = getTotalMemory();
        if (total == 0) {
            return 0.0;
        }
        return (double) getUsedMemory() / total * 100;
    }

    /**
     * Heap 메모리 사용량 (바이트)
     */
    public static long getHeapMemoryUsed() {
        return MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed();
    }

    /**
     * Heap 메모리 사용량 (MB)
     */
    public static long getHeapMemoryUsedMb() {
        return getHeapMemoryUsed() / (1024 * 1024);
    }

    /**
     * Non-Heap 메모리 사용량 (바이트)
     */
    public static long getNonHeapMemoryUsed() {
        return MEMORY_MX_BEAN.getNonHeapMemoryUsage().getUsed();
    }

    /**
     * Non-Heap 메모리 사용량 (MB)
     */
    public static long getNonHeapMemoryUsedMb() {
        return getNonHeapMemoryUsed() / (1024 * 1024);
    }

    // ===== CPU 정보 =====

    /**
     * 사용 가능한 CPU 코어 수
     */
    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * CPU 개수 (코어 수)
     */
    public static int getCpuCount() {
        return getAvailableProcessors();
    }

    // ===== 시스템 속성 =====

    /**
     * 사용자 이름
     */
    public static String getUserName() {
        return System.getProperty("user.name");
    }

    /**
     * 사용자 홈 디렉토리
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * 현재 작업 디렉토리
     */
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 임시 디렉토리
     */
    public static String getTempDir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 파일 구분자
     * 예: "/" (Unix), "\\" (Windows)
     */
    public static String getFileSeparator() {
        return File.separator;
    }

    /**
     * 경로 구분자
     * 예: ":" (Unix), ";" (Windows)
     */
    public static String getPathSeparator() {
        return File.pathSeparator;
    }

    /**
     * 줄 구분자
     * 예: "\n" (Unix), "\r\n" (Windows)
     */
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * 파일 인코딩
     * 예: "UTF-8"
     */
    public static String getFileEncoding() {
        return System.getProperty("file.encoding");
    }

    // ===== 네트워크 정보 =====

    /**
     * 호스트 이름 반환
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    /**
     * 호스트 IP 주소 반환
     */
    public static String getHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "Unknown";
        }
    }

    // ===== 디스크 정보 =====

    /**
     * 루트 디렉토리의 전체 용량 (바이트)
     */
    public static long getTotalDiskSpace() {
        File root = new File("/");
        return root.getTotalSpace();
    }

    /**
     * 루트 디렉토리의 전체 용량 (GB)
     */
    public static long getTotalDiskSpaceGb() {
        return getTotalDiskSpace() / (1024 * 1024 * 1024);
    }

    /**
     * 루트 디렉토리의 여유 공간 (바이트)
     */
    public static long getFreeDiskSpace() {
        File root = new File("/");
        return root.getFreeSpace();
    }

    /**
     * 루트 디렉토리의 여유 공간 (GB)
     */
    public static long getFreeDiskSpaceGb() {
        return getFreeDiskSpace() / (1024 * 1024 * 1024);
    }

    /**
     * 루트 디렉토리의 사용 가능 공간 (바이트)
     */
    public static long getUsableDiskSpace() {
        File root = new File("/");
        return root.getUsableSpace();
    }

    /**
     * 루트 디렉토리의 사용 가능 공간 (GB)
     */
    public static long getUsableDiskSpaceGb() {
        return getUsableDiskSpace() / (1024 * 1024 * 1024);
    }

    /**
     * 특정 경로의 전체 용량 (바이트)
     */
    public static long getTotalDiskSpace(String path) {
        File file = new File(path);
        return file.getTotalSpace();
    }

    /**
     * 특정 경로의 여유 공간 (바이트)
     */
    public static long getFreeDiskSpace(String path) {
        File file = new File(path);
        return file.getFreeSpace();
    }

    // ===== 프로세스 정보 =====

    /**
     * 현재 프로세스 ID (PID)
     */
    public static long getProcessId() {
        return ProcessHandle.current().pid();
    }

    /**
     * JVM 입력 인자 반환
     * 예: ["-Xmx1024m", "-Dspring.profiles.active=dev"]
     */
    public static java.util.List<String> getJvmArguments() {
        return RUNTIME_MX_BEAN.getInputArguments();
    }

    // ===== 환경 변수 =====

    /**
     * 환경 변수 값 조회
     */
    public static String getEnv(String key) {
        return System.getenv(key);
    }

    /**
     * 환경 변수 값 조회 (기본값 지정)
     */
    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 모든 환경 변수 반환
     */
    public static java.util.Map<String, String> getAllEnv() {
        return System.getenv();
    }

    // ===== 시스템 속성 =====

    /**
     * 시스템 속성 값 조회
     */
    public static String getProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 시스템 속성 값 조회 (기본값 지정)
     */
    public static String getProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }

    /**
     * 모든 시스템 속성 반환
     */
    public static Properties getAllProperties() {
        return System.getProperties();
    }

    // ===== 유틸리티 =====

    /**
     * 시스템 정보를 문자열로 출력 (디버깅용)
     */
    public static String getSystemInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== System Information ===\n");
        sb.append("OS: ").append(getOsName()).append(" ").append(getOsVersion()).append("\n");
        sb.append("Architecture: ").append(getOsArch()).append("\n");
        sb.append("Java Version: ").append(getJavaVersion()).append("\n");
        sb.append("Java Home: ").append(getJavaHome()).append("\n");
        sb.append("JVM: ").append(getJvmName()).append("\n");
        sb.append("User: ").append(getUserName()).append("\n");
        sb.append("Hostname: ").append(getHostname()).append("\n");
        sb.append("Host Address: ").append(getHostAddress()).append("\n");
        sb.append("CPU Cores: ").append(getCpuCount()).append("\n");
        sb.append("Max Memory: ").append(getMaxMemoryMb()).append(" MB\n");
        sb.append("Total Memory: ").append(getTotalMemoryMb()).append(" MB\n");
        sb.append("Used Memory: ").append(getUsedMemoryMb()).append(" MB\n");
        sb.append("Free Memory: ").append(getFreeMemoryMb()).append(" MB\n");
        sb.append("Memory Usage: ").append(String.format("%.2f", getMemoryUsagePercent())).append("%\n");
        sb.append("Process ID: ").append(getProcessId()).append("\n");
        sb.append("JVM Uptime: ").append(getJvmUptime()).append(" ms\n");
        return sb.toString();
    }

    /**
     * 메모리 정보를 문자열로 출력
     */
    public static String getMemoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Memory Information ===\n");
        sb.append("Max Memory: ").append(getMaxMemoryMb()).append(" MB\n");
        sb.append("Total Memory: ").append(getTotalMemoryMb()).append(" MB\n");
        sb.append("Used Memory: ").append(getUsedMemoryMb()).append(" MB\n");
        sb.append("Free Memory: ").append(getFreeMemoryMb()).append(" MB\n");
        sb.append("Memory Usage: ").append(String.format("%.2f", getMemoryUsagePercent())).append("%\n");
        sb.append("Heap Memory Used: ").append(getHeapMemoryUsedMb()).append(" MB\n");
        sb.append("Non-Heap Memory Used: ").append(getNonHeapMemoryUsedMb()).append(" MB\n");
        return sb.toString();
    }

    /**
     * Garbage Collection 실행 요청
     */
    public static void gc() {
        System.gc();
    }

    /**
     * 애플리케이션 종료
     */
    public static void exit(int status) {
        System.exit(status);
    }

    /**
     * 현재 시간 (나노초)
     */
    public static long nanoTime() {
        return System.nanoTime();
    }

    /**
     * 현재 시간 (밀리초)
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
