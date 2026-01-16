package com.eraf.starter.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * ERAF 범용 아이템 프로세서
 * 함수형 인터페이스를 활용한 유연한 데이터 변환
 *
 * @param <I> 입력 타입
 * @param <O> 출력 타입
 */
public class ErafItemProcessor<I, O> implements ItemProcessor<I, O> {

    private static final Logger log = LoggerFactory.getLogger(ErafItemProcessor.class);

    private final Function<I, O> transformer;
    private final Predicate<I> filter;
    private final String processorName;

    /**
     * 변환 함수만 사용하는 생성자
     */
    public ErafItemProcessor(Function<I, O> transformer) {
        this("default", transformer, item -> true);
    }

    /**
     * 프로세서 이름과 변환 함수를 사용하는 생성자
     */
    public ErafItemProcessor(String processorName, Function<I, O> transformer) {
        this(processorName, transformer, item -> true);
    }

    /**
     * 전체 파라미터 생성자
     */
    public ErafItemProcessor(String processorName, Function<I, O> transformer, Predicate<I> filter) {
        this.processorName = processorName;
        this.transformer = transformer;
        this.filter = filter;
    }

    @Override
    public O process(I item) throws Exception {
        if (item == null) {
            return null;
        }

        // 필터 조건 확인
        if (!filter.test(item)) {
            log.debug("[{}] Item filtered out: {}", processorName, item);
            return null;
        }

        try {
            O result = transformer.apply(item);
            log.trace("[{}] Item processed: {} -> {}", processorName, item, result);
            return result;
        } catch (Exception e) {
            log.error("[{}] Error processing item: {}", processorName, item, e);
            throw e;
        }
    }

    /**
     * 빌더 패턴으로 프로세서 생성
     */
    public static <I, O> Builder<I, O> builder() {
        return new Builder<>();
    }

    public static class Builder<I, O> {
        private String name = "processor";
        private Function<I, O> transformer;
        private Predicate<I> filter = item -> true;

        public Builder<I, O> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<I, O> transformer(Function<I, O> transformer) {
            this.transformer = transformer;
            return this;
        }

        public Builder<I, O> filter(Predicate<I> filter) {
            this.filter = filter;
            return this;
        }

        public ErafItemProcessor<I, O> build() {
            if (transformer == null) {
                throw new IllegalStateException("Transformer must be set");
            }
            return new ErafItemProcessor<>(name, transformer, filter);
        }
    }
}
