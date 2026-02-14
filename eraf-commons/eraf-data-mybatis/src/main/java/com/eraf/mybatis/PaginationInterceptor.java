package com.eraf.mybatis;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MyBatis 자동 페이지네이션 인터셉터
 *
 * RowBounds가 기본값이 아닌 쿼리를 가로채서
 * SQL에 LIMIT/OFFSET을 자동으로 추가합니다.
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class})
})
public class PaginationInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(PaginationInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        RowBounds rowBounds = (RowBounds) args[2];

        // RowBounds가 기본값이면 페이지네이션 적용하지 않음
        if (rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        }

        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql;

        if (args.length == 6) {
            boundSql = (BoundSql) args[5];
        } else {
            boundSql = ms.getBoundSql(parameter);
        }

        String originalSql = boundSql.getSql().trim();

        // LIMIT/OFFSET이 이미 포함된 경우 건너뜀
        if (originalSql.toUpperCase().contains("LIMIT")) {
            return invocation.proceed();
        }

        int offset = rowBounds.getOffset();
        int limit = rowBounds.getLimit();

        String paginatedSql = originalSql + " LIMIT " + limit + " OFFSET " + offset;

        log.debug("Pagination applied - offset: {}, limit: {}, statement: {}",
                offset, limit, ms.getId());

        // 수정된 BoundSql로 새 MappedStatement 생성
        BoundSql newBoundSql = new BoundSql(
                ms.getConfiguration(), paginatedSql,
                boundSql.getParameterMappings(), boundSql.getParameterObject());

        // 추가 파라미터 복사
        boundSql.getParameterMappings().forEach(pm -> {
            String prop = pm.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        });

        MappedStatement newMs = copyMappedStatement(ms, newBoundSql);

        args[0] = newMs;
        // RowBounds를 기본값으로 리셋하여 MyBatis 내부 페이지네이션 방지
        args[2] = RowBounds.DEFAULT;

        return invocation.proceed();
    }

    /**
     * BoundSql이 교체된 MappedStatement 복사본 생성
     */
    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSql newBoundSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(
                ms.getConfiguration(), ms.getId(),
                parameter -> newBoundSql,
                ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null) {
            builder.keyProperty(String.join(",", ms.getKeyProperties()));
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }
}
