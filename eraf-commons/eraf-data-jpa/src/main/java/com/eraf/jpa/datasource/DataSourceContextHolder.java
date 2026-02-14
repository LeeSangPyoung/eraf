package com.eraf.jpa.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataSource 컨텍스트 관리
 *
 * ThreadLocal을 사용하여 현재 스레드의 DataSource 키를 관리합니다.
 */
public class DataSourceContextHolder {

    private static final Logger log = LoggerFactory.getLogger(DataSourceContextHolder.class);
    private static final ThreadLocal<String> contextHolder = new InheritableThreadLocal<>();

    /**
     * 현재 DataSource 키 조회
     */
    public static String getCurrentDataSource() {
        return contextHolder.get();
    }

    /**
     * DataSource 키 설정
     */
    public static void setDataSource(String dataSourceKey) {
        log.debug("Switching DataSource to: {}", dataSourceKey);
        contextHolder.set(dataSourceKey);
    }

    /**
     * DataSource 컨텍스트 초기화
     */
    public static void clear() {
        contextHolder.remove();
    }
}
