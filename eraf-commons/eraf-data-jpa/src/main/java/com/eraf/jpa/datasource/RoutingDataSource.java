package com.eraf.jpa.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 동적 DataSource 라우팅
 *
 * ThreadLocal 기반으로 현재 요청의 DataSource 키를 결정합니다.
 * Multi-tenancy에서 테넌트별 DB 분리 시 사용합니다.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getCurrentDataSource();
    }
}
