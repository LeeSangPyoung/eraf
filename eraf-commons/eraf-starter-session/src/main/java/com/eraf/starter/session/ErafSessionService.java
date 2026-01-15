package com.eraf.starter.session;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * ERAF 세션 서비스
 * Redis 기반 세션 관리 + 동시 세션 제어
 */
public class ErafSessionService {

    private static final String USER_SESSIONS_KEY = "user:sessions:";
    private static final String SESSION_USER_KEY = "session:user:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ErafSessionProperties properties;

    public ErafSessionService(RedisTemplate<String, String> redisTemplate, ErafSessionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    /**
     * 사용자 세션 등록
     * 동시 세션 정책(kick-old)에 따라 기존 세션 무효화
     */
    public void registerSession(String userId, String sessionId) {
        String namespace = properties.getRedisNamespace();
        String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY + userId;
        String sessionUserKey = namespace + ":" + SESSION_USER_KEY + sessionId;
        Duration timeout = properties.getTimeout();

        // 기존 세션 조회
        Set<String> existingSessions = redisTemplate.opsForSet().members(userSessionsKey);

        // kick-old 정책: 기존 세션들 무효화
        if (existingSessions != null && !existingSessions.isEmpty()) {
            int maxSessions = properties.getConcurrentSession().getMaxSessions();
            if (existingSessions.size() >= maxSessions) {
                for (String oldSessionId : existingSessions) {
                    invalidateSession(oldSessionId);
                }
                redisTemplate.delete(userSessionsKey);
            }
        }

        // 새 세션 등록
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, timeout.toSeconds(), TimeUnit.SECONDS);

        // 세션-사용자 매핑 저장
        redisTemplate.opsForValue().set(sessionUserKey, userId, timeout.toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 세션 무효화
     */
    public void invalidateSession(String sessionId) {
        String namespace = properties.getRedisNamespace();
        String sessionUserKey = namespace + ":" + SESSION_USER_KEY + sessionId;

        // 세션에 연결된 사용자 조회
        String userId = redisTemplate.opsForValue().get(sessionUserKey);

        if (userId != null) {
            String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY + userId;
            redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
        }

        // 세션-사용자 매핑 삭제
        redisTemplate.delete(sessionUserKey);

        // 세션 데이터 삭제 (Spring Session 형식)
        redisTemplate.delete(namespace + ":sessions:" + sessionId);
    }

    /**
     * 사용자의 모든 세션 무효화 (로그아웃 등)
     */
    public void invalidateAllSessions(String userId) {
        String namespace = properties.getRedisNamespace();
        String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY + userId;

        Set<String> sessions = redisTemplate.opsForSet().members(userSessionsKey);
        if (sessions != null) {
            for (String sessionId : sessions) {
                invalidateSession(sessionId);
            }
        }

        redisTemplate.delete(userSessionsKey);
    }

    /**
     * 세션 유효성 확인
     */
    public boolean isSessionValid(String sessionId) {
        String namespace = properties.getRedisNamespace();
        String sessionUserKey = namespace + ":" + SESSION_USER_KEY + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionUserKey));
    }

    /**
     * 세션 연장
     */
    public void refreshSession(String sessionId) {
        String namespace = properties.getRedisNamespace();
        String sessionUserKey = namespace + ":" + SESSION_USER_KEY + sessionId;
        Duration timeout = properties.getTimeout();

        String userId = redisTemplate.opsForValue().get(sessionUserKey);
        if (userId != null) {
            String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY + userId;

            redisTemplate.expire(sessionUserKey, timeout.toSeconds(), TimeUnit.SECONDS);
            redisTemplate.expire(userSessionsKey, timeout.toSeconds(), TimeUnit.SECONDS);
        }
    }

    /**
     * 사용자의 활성 세션 수 조회
     */
    public long getActiveSessionCount(String userId) {
        String namespace = properties.getRedisNamespace();
        String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY + userId;

        Long count = redisTemplate.opsForSet().size(userSessionsKey);
        return count != null ? count : 0;
    }
}
