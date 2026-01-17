package com.eraf.core.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

/**
 * 예외 처리 유틸리티
 * 예외 분석, 스택 트레이스 추출, 원인 예외 찾기 등
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    // ===== 스택 트레이스 =====

    /**
     * 예외의 전체 스택 트레이스를 문자열로 변환
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * 예외의 짧은 스택 트레이스 (상위 N개 라인만)
     */
    public static String getStackTrace(Throwable throwable, int maxLines) {
        if (throwable == null) {
            return null;
        }
        String fullTrace = getStackTrace(throwable);
        String[] lines = fullTrace.split("\n");
        if (lines.length <= maxLines) {
            return fullTrace;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLines; i++) {
            sb.append(lines[i]).append("\n");
        }
        sb.append("... (").append(lines.length - maxLines).append(" more lines)");
        return sb.toString();
    }

    /**
     * 스택 트레이스 요소 배열 반환
     */
    public static StackTraceElement[] getStackTraceElements(Throwable throwable) {
        if (throwable == null) {
            return new StackTraceElement[0];
        }
        return throwable.getStackTrace();
    }

    /**
     * 스택 트레이스를 문자열 리스트로 변환
     */
    public static List<String> getStackTraceList(Throwable throwable) {
        List<String> list = new ArrayList<>();
        if (throwable == null) {
            return list;
        }
        StackTraceElement[] elements = throwable.getStackTrace();
        for (StackTraceElement element : elements) {
            list.add(element.toString());
        }
        return list;
    }

    // ===== 원인 예외 (Root Cause) =====

    /**
     * 가장 근본적인 원인 예외 반환
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /**
     * 원인 예외의 메시지 반환
     */
    public static String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        return rootCause != null ? rootCause.getMessage() : null;
    }

    /**
     * 원인 예외의 스택 트레이스 반환
     */
    public static String getRootCauseStackTrace(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        return getStackTrace(rootCause);
    }

    // ===== 예외 체인 =====

    /**
     * 예외 체인의 모든 예외를 리스트로 반환
     * [현재 예외, 원인 예외, 원인의 원인 예외, ...]
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }

    /**
     * 예외 체인의 개수 반환
     */
    public static int getThrowableCount(Throwable throwable) {
        return getThrowableList(throwable).size();
    }

    /**
     * 예외 체인에서 특정 타입의 예외 찾기
     */
    public static <T extends Throwable> T getThrowableByType(Throwable throwable, Class<T> type) {
        if (throwable == null || type == null) {
            return null;
        }
        List<Throwable> throwables = getThrowableList(throwable);
        for (Throwable t : throwables) {
            if (type.isInstance(t)) {
                return type.cast(t);
            }
        }
        return null;
    }

    /**
     * 예외 체인에 특정 타입의 예외가 있는지 확인
     */
    public static boolean containsType(Throwable throwable, Class<? extends Throwable> type) {
        return getThrowableByType(throwable, type) != null;
    }

    // ===== 메시지 추출 =====

    /**
     * 예외 메시지 반환 (null-safe)
     */
    public static String getMessage(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return throwable.getMessage();
    }

    /**
     * 예외 체인의 모든 메시지를 연결하여 반환
     */
    public static String getAllMessages(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        List<Throwable> throwables = getThrowableList(throwable);
        for (int i = 0; i < throwables.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            String message = throwables.get(i).getMessage();
            if (message != null) {
                sb.append(message);
            } else {
                sb.append(throwables.get(i).getClass().getSimpleName());
            }
        }
        return sb.toString();
    }

    /**
     * 예외 체인의 모든 메시지를 리스트로 반환
     */
    public static List<String> getMessageList(Throwable throwable) {
        List<String> messages = new ArrayList<>();
        if (throwable == null) {
            return messages;
        }
        List<Throwable> throwables = getThrowableList(throwable);
        for (Throwable t : throwables) {
            String message = t.getMessage();
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    // ===== 예외 래핑 해제 =====

    /**
     * InvocationTargetException 래핑 해제
     */
    public static Throwable unwrapInvocationTargetException(Throwable throwable) {
        if (throwable instanceof InvocationTargetException) {
            InvocationTargetException ite = (InvocationTargetException) throwable;
            return ite.getTargetException() != null ? ite.getTargetException() : throwable;
        }
        return throwable;
    }

    /**
     * UndeclaredThrowableException 래핑 해제
     */
    public static Throwable unwrapUndeclaredThrowableException(Throwable throwable) {
        if (throwable instanceof UndeclaredThrowableException) {
            UndeclaredThrowableException ute = (UndeclaredThrowableException) throwable;
            return ute.getUndeclaredThrowable() != null ? ute.getUndeclaredThrowable() : throwable;
        }
        return throwable;
    }

    /**
     * 모든 래핑 해제
     */
    public static Throwable unwrap(Throwable throwable) {
        Throwable unwrapped = throwable;
        unwrapped = unwrapInvocationTargetException(unwrapped);
        unwrapped = unwrapUndeclaredThrowableException(unwrapped);
        return unwrapped;
    }

    // ===== 예외 타입 체크 =====

    /**
     * RuntimeException 또는 그 하위 클래스인지 확인
     */
    public static boolean isRuntimeException(Throwable throwable) {
        return throwable instanceof RuntimeException;
    }

    /**
     * Checked Exception인지 확인 (RuntimeException이나 Error가 아닌 경우)
     */
    public static boolean isCheckedException(Throwable throwable) {
        return throwable != null &&
               !(throwable instanceof RuntimeException) &&
               !(throwable instanceof Error);
    }

    /**
     * Error 또는 그 하위 클래스인지 확인
     */
    public static boolean isError(Throwable throwable) {
        return throwable instanceof Error;
    }

    // ===== 예외 던지기 =====

    /**
     * Checked Exception을 Unchecked Exception으로 변환하여 던지기
     */
    public static RuntimeException wrapAndThrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        throw new RuntimeException(throwable);
    }

    /**
     * 예외를 RuntimeException으로 래핑 (던지지 않음)
     */
    public static RuntimeException wrap(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(throwable);
    }

    /**
     * 예외를 RuntimeException으로 래핑 (메시지 포함)
     */
    public static RuntimeException wrap(String message, Throwable throwable) {
        if (throwable instanceof RuntimeException && message == null) {
            return (RuntimeException) throwable;
        }
        return new RuntimeException(message, throwable);
    }

    // ===== 예외 정보 =====

    /**
     * 예외 클래스명 반환
     */
    public static String getClassName(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return throwable.getClass().getName();
    }

    /**
     * 예외 심플 클래스명 반환
     */
    public static String getSimpleClassName(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return throwable.getClass().getSimpleName();
    }

    /**
     * 예외가 발생한 메서드 정보 반환
     */
    public static String getThrowingMethod(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StackTraceElement[] elements = throwable.getStackTrace();
        if (elements.length > 0) {
            StackTraceElement first = elements[0];
            return first.getClassName() + "." + first.getMethodName() +
                   "(" + first.getFileName() + ":" + first.getLineNumber() + ")";
        }
        return null;
    }

    // ===== 예외 비교 =====

    /**
     * 두 예외가 같은 타입인지 확인
     */
    public static boolean isSameType(Throwable t1, Throwable t2) {
        if (t1 == null || t2 == null) {
            return t1 == t2;
        }
        return t1.getClass().equals(t2.getClass());
    }

    /**
     * 두 예외의 메시지가 같은지 확인
     */
    public static boolean isSameMessage(Throwable t1, Throwable t2) {
        String msg1 = getMessage(t1);
        String msg2 = getMessage(t2);
        if (msg1 == null && msg2 == null) {
            return true;
        }
        if (msg1 == null || msg2 == null) {
            return false;
        }
        return msg1.equals(msg2);
    }

    // ===== 디버깅 =====

    /**
     * 예외의 간단한 요약 정보 반환
     */
    public static String getSummary(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSimpleClassName(throwable));
        String message = getMessage(throwable);
        if (message != null) {
            sb.append(": ").append(message);
        }
        String method = getThrowingMethod(throwable);
        if (method != null) {
            sb.append(" at ").append(method);
        }
        return sb.toString();
    }

    /**
     * 예외 체인의 모든 요약 정보 반환
     */
    public static String getAllSummaries(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        List<Throwable> throwables = getThrowableList(throwable);
        for (int i = 0; i < throwables.size(); i++) {
            if (i > 0) {
                sb.append("\nCaused by: ");
            }
            sb.append(getSummary(throwables.get(i)));
        }
        return sb.toString();
    }

    // ===== 예외 메시지 포맷팅 =====

    /**
     * 예외를 사용자 친화적인 메시지로 변환
     */
    public static String toUserFriendlyMessage(Throwable throwable) {
        if (throwable == null) {
            return "알 수 없는 오류가 발생했습니다.";
        }
        String message = getMessage(throwable);
        if (message == null || message.isEmpty()) {
            return getSimpleClassName(throwable) + " 오류가 발생했습니다.";
        }
        return message;
    }

    /**
     * 예외를 개발자용 메시지로 변환
     */
    public static String toDeveloperMessage(Throwable throwable) {
        if (throwable == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getClassName(throwable)).append("] ");
        String message = getMessage(throwable);
        if (message != null) {
            sb.append(message);
        }
        sb.append("\n");
        sb.append("Root Cause: ").append(getRootCauseMessage(throwable));
        sb.append("\n");
        sb.append("Thrown at: ").append(getThrowingMethod(throwable));
        return sb.toString();
    }

    // ===== null-safe 실행 =====

    /**
     * 예외를 무시하고 실행 (예외 발생 시 null 반환)
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /**
     * 예외를 무시하고 실행 (예외 발생 시 null 반환)
     */
    public static <T> T ignoreException(ThrowingSupplier<T> supplier) {
        return ignoreException(supplier, null);
    }

    /**
     * 예외를 무시하고 실행 (예외 발생 시 defaultValue 반환)
     */
    public static <T> T ignoreException(ThrowingSupplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 예외를 무시하고 실행 (예외 무시)
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * 예외를 무시하고 실행
     */
    public static void ignoreException(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            // 무시
        }
    }
}
