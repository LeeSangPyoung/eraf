package com.eraf.core.idempotent;

import com.eraf.core.crypto.Hash;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 멱등성 키 생성기
 */
public final class IdempotencyKeyGenerator {

    private static final String PREFIX = "idempotent:";

    private IdempotencyKeyGenerator() {
    }

    /**
     * 메서드와 파라미터로 키 생성
     */
    public static String generate(Method method, Object[] args) {
        String methodKey = method.getDeclaringClass().getName() + "." + method.getName();
        String argsKey = args != null ?
                Arrays.stream(args)
                        .map(arg -> arg != null ? arg.toString() : "null")
                        .collect(Collectors.joining(":")) :
                "";
        return PREFIX + Hash.hash(methodKey + ":" + argsKey);
    }

    /**
     * 커스텀 키로 생성
     */
    public static String generate(String customKey) {
        return PREFIX + Hash.hash(customKey);
    }

    /**
     * 클래스, 메서드명, 파라미터로 키 생성
     */
    public static String generate(String className, String methodName, Object... args) {
        String methodKey = className + "." + methodName;
        String argsKey = args != null ?
                Arrays.stream(args)
                        .map(arg -> arg != null ? arg.toString() : "null")
                        .collect(Collectors.joining(":")) :
                "";
        return PREFIX + Hash.hash(methodKey + ":" + argsKey);
    }

    /**
     * 요청 ID 기반 키 생성
     */
    public static String fromRequestId(String requestId) {
        return PREFIX + "req:" + requestId;
    }

    /**
     * 사용자별 키 생성
     */
    public static String forUser(String userId, String operation) {
        return PREFIX + "user:" + userId + ":" + Hash.hash(operation);
    }
}
