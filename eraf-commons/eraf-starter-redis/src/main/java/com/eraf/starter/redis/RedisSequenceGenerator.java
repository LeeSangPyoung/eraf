package com.eraf.starter.redis;

import com.eraf.core.sequence.Reset;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 채번 생성기
 * 분산 환경에서 고유한 시퀀스 번호 생성
 */
public class RedisSequenceGenerator {

    private static final String SEQUENCE_PREFIX = "eraf:sequence:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    private final StringRedisTemplate redisTemplate;

    public RedisSequenceGenerator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 시퀀스 값 생성 (리셋 없음)
     */
    public long nextValue(String name) {
        String key = SEQUENCE_PREFIX + name;
        Long value = redisTemplate.opsForValue().increment(key);
        return value != null ? value : 1L;
    }

    /**
     * 시퀀스 값 생성 (리셋 주기 지정)
     */
    public long nextValue(String name, Reset reset) {
        String key = generateKey(name, reset);
        Long value = redisTemplate.opsForValue().increment(key);

        // 새로 생성된 키면 만료 시간 설정
        if (value != null && value == 1L) {
            setExpiration(key, reset);
        }

        return value != null ? value : 1L;
    }

    /**
     * 포맷된 시퀀스 문자열 생성
     * 예: "ORD-20240115-00001"
     */
    public String nextFormatted(String prefix, String name, Reset reset, int digits) {
        long value = nextValue(name, reset);
        String datePart = getDatePart(reset);
        String sequencePart = String.format("%0" + digits + "d", value);

        return prefix + "-" + datePart + "-" + sequencePart;
    }

    /**
     * 포맷된 시퀀스 문자열 생성 (간단 버전)
     * 예: "00001"
     */
    public String nextFormatted(String name, int digits) {
        long value = nextValue(name);
        return String.format("%0" + digits + "d", value);
    }

    /**
     * 현재 시퀀스 값 조회 (증가 없이)
     */
    public long currentValue(String name) {
        String key = SEQUENCE_PREFIX + name;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 현재 시퀀스 값 조회 (리셋 주기 지정)
     */
    public long currentValue(String name, Reset reset) {
        String key = generateKey(name, reset);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 시퀀스 리셋
     */
    public void reset(String name) {
        String key = SEQUENCE_PREFIX + name;
        redisTemplate.delete(key);
    }

    /**
     * 시퀀스 초기값 설정
     */
    public void setInitialValue(String name, long value) {
        String key = SEQUENCE_PREFIX + name;
        redisTemplate.opsForValue().set(key, String.valueOf(value));
    }

    private String generateKey(String name, Reset reset) {
        if (reset == Reset.DAILY) {
            return SEQUENCE_PREFIX + name + ":" + LocalDate.now().format(DATE_FORMATTER);
        } else if (reset == Reset.MONTHLY) {
            return SEQUENCE_PREFIX + name + ":" + LocalDate.now().format(MONTH_FORMATTER);
        } else if (reset == Reset.YEARLY) {
            return SEQUENCE_PREFIX + name + ":" + LocalDate.now().format(YEAR_FORMATTER);
        } else {
            return SEQUENCE_PREFIX + name;
        }
    }

    private String getDatePart(Reset reset) {
        if (reset == Reset.DAILY) {
            return LocalDate.now().format(DATE_FORMATTER);
        } else if (reset == Reset.MONTHLY) {
            return LocalDate.now().format(MONTH_FORMATTER);
        } else if (reset == Reset.YEARLY) {
            return LocalDate.now().format(YEAR_FORMATTER);
        } else {
            return "";
        }
    }

    private void setExpiration(String key, Reset reset) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = null;

        if (reset == Reset.DAILY) {
            expireAt = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
        } else if (reset == Reset.MONTHLY) {
            expireAt = now.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        } else if (reset == Reset.YEARLY) {
            expireAt = now.plusYears(1).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
        }

        if (expireAt != null) {
            long ttlSeconds = ChronoUnit.SECONDS.between(now, expireAt);
            // 약간의 여유를 두고 만료 시간 설정
            redisTemplate.expire(key, ttlSeconds + 60, TimeUnit.SECONDS);
        }
    }
}
