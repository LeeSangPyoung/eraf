package com.eraf.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
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
 * @Timed 어노테이션 처리 Aspect
 */
@Aspect
public class TimedAspect {

    private static final Logger log = LoggerFactory.getLogger(TimedAspect.class);

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    public TimedAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(timed)")
    public Object around(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        String metricName = resolveMetricName(joinPoint, timed);
        List<Tag> tags = resolveTags(joinPoint, timed);

        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";

        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            outcome = "error";
            if (!timed.recordOnException()) {
                throw t;
            }
            throw t;
        } finally {
            tags.add(Tag.of("outcome", outcome));
            Timer timer = getOrCreateTimer(metricName, timed, tags);
            sample.stop(timer);
        }
    }

    private String resolveMetricName(ProceedingJoinPoint joinPoint, Timed timed) {
        if (!timed.value().isEmpty()) {
            return timed.value();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
    }

    private List<Tag> resolveTags(ProceedingJoinPoint joinPoint, Timed timed) {
        List<Tag> tags = new ArrayList<>();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        tags.add(Tag.of("class", signature.getDeclaringType().getSimpleName()));
        tags.add(Tag.of("method", signature.getMethod().getName()));

        // Parse extra tags
        for (String extraTag : timed.extraTags()) {
            String[] parts = extraTag.split("=", 2);
            if (parts.length == 2) {
                tags.add(Tag.of(parts[0], parts[1]));
            }
        }

        return tags;
    }

    private Timer getOrCreateTimer(String name, Timed timed, List<Tag> tags) {
        String cacheKey = name + tags.toString();
        return timerCache.computeIfAbsent(cacheKey, k -> {
            Timer.Builder builder = Timer.builder(name)
                    .tags(tags)
                    .description(timed.description().isEmpty() ? null : timed.description());

            if (timed.histogram()) {
                builder.publishPercentileHistogram();
            }

            if (timed.percentiles().length > 0) {
                builder.publishPercentiles(timed.percentiles());
            }

            return builder.register(meterRegistry);
        });
    }
}
