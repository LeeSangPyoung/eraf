package com.eraf.core.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * 비밀번호 해싱 (bcrypt 고정)
 */
public final class Password {

    private static final int DEFAULT_COST = 12;

    private Password() {
    }

    /**
     * 비밀번호 해시 생성
     *
     * @param password 평문 비밀번호
     * @return bcrypt 해시
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 null일 수 없습니다");
        }
        return BCrypt.withDefaults().hashToString(DEFAULT_COST, password.toCharArray());
    }

    /**
     * 비밀번호 해시 생성 (cost 지정)
     *
     * @param password 평문 비밀번호
     * @param cost     bcrypt cost factor (4-31)
     * @return bcrypt 해시
     */
    public static String hash(String password, int cost) {
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 null일 수 없습니다");
        }
        if (cost < 4 || cost > 31) {
            throw new IllegalArgumentException("cost는 4에서 31 사이여야 합니다");
        }
        return BCrypt.withDefaults().hashToString(cost, password.toCharArray());
    }

    /**
     * 비밀번호 검증
     *
     * @param password 평문 비밀번호
     * @param hash     저장된 bcrypt 해시
     * @return 일치 여부
     */
    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hash);
        return result.verified;
    }

    /**
     * 해시 업그레이드 필요 여부 확인
     * (cost factor가 변경된 경우 재해싱 권장)
     *
     * @param hash 저장된 bcrypt 해시
     * @return 업그레이드 필요 여부
     */
    public static boolean needsRehash(String hash) {
        if (hash == null) {
            return true;
        }
        try {
            // bcrypt 해시 형식: $2a$12$...
            String[] parts = hash.split("\\$");
            if (parts.length < 4) {
                return true;
            }
            int cost = Integer.parseInt(parts[2]);
            return cost < DEFAULT_COST;
        } catch (Exception e) {
            return true;
        }
    }
}
