package com.eraf.core.context;

/**
 * ERAF 컨텍스트 홀더
 * ThreadLocal 기반 컨텍스트 관리
 */
public final class ErafContextHolder {

    private static final ThreadLocal<ErafContext> contextHolder = ThreadLocal.withInitial(ErafContext::new);

    private ErafContextHolder() {
        // Utility class
    }

    /**
     * 현재 스레드의 컨텍스트 조회
     */
    public static ErafContext getContext() {
        return contextHolder.get();
    }

    /**
     * 현재 스레드에 컨텍스트 설정
     */
    public static void setContext(ErafContext context) {
        if (context == null) {
            clearContext();
        } else {
            contextHolder.set(context);
        }
    }

    /**
     * 현재 스레드의 컨텍스트 초기화
     */
    public static void clearContext() {
        contextHolder.remove();
    }

    /**
     * 새 컨텍스트 생성 및 설정
     */
    public static ErafContext createContext() {
        ErafContext context = new ErafContext();
        contextHolder.set(context);
        return context;
    }
}
