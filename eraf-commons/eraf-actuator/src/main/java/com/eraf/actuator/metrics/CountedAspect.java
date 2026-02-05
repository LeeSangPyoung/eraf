package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @Counted 어노테이션 처리 Aspect
 */
@Aspect
public class CountedAspect {

    private static final Logger log = LoggerFactory.getLogger(CountedAspect.class);

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counterCache = new ConcurrentHashMap<>();

    public CountedAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(counted)")
    public Object around(ProceedingJoinPoint joinPoint, Counted counted) throws Throwable {
        String metricName = resolveMetricName(joinPoint, counted);

        try {
            Object result = joinPoint.proceed();

            List<Tag> tags = resolveTags(joinPoint, counted);
            tags.add(Tag.of("outcome", "success"));
            incrementCounter(metricName, counted, tags);

            return result;
        } catch (Throwable t) {
            if (counted.recordOnException()) {
                List<Tag> tags = resolveTags(joinPoint, counted);
                tags.add(Tag.of("outcome", "error"));
                tags.add(Tag.of("exception", t.getClass().getSimpleName()));
                incrementCounter(metricName, counted, tags);
            }
            throw t;
        }
    }

    private String resolveMetricName(ProceedingJoinPoint joinPoint, Counted counted) {
        if (!counted.value().isEmpty()) {
            return counted.value();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName() + ".count";
    }

    private List<Tag> resolveTags(ProceedingJoinPoint joinPoint, Counted counted) {
        List<Tag> tags = new ArrayList<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        tags.add(Tag.of("class", signature.getDeclaringType().getSimpleName()));
        tags.add(Tag.of("method", signature.getMethod().getName()));

        // Parse extra tags
        for (String extraTag : counted.extraTags()) {
            String[] parts = extraTag.split("=", 2);
            if (parts.length == 2) {
                tags.add(Tag.of(parts[0], parts[1]));
            }
        }

        return tags;
    }

    private void incrementCounter(String name, Counted counted, List<Tag> tags) {
        String cacheKey = name + tags.toString();
        Counter counter = counterCache.computeIfAbsent(cacheKey, k ->
                Counter.builder(name)
                        .tags(tags)
                        .description(counted.description().isEmpty() ? null : counted.description())
                        .register(meterRegistry)
        );
        counter.increment();
    }
}
