package com.eraf.starter.jpa.audit;

import com.eraf.core.context.ErafContext;
import com.eraf.core.context.ErafContextHolder;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * ERAF 감사 정보 제공자
 * ErafContext에서 현재 사용자 정보를 가져옴
 */
public class ErafAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_USER = "system";

    @Override
    public Optional<String> getCurrentAuditor() {
        ErafContext context = ErafContextHolder.getContext();

        if (context != null) {
            String userId = context.getUserId();
            if (userId != null && !userId.isEmpty()) {
                return Optional.of(userId);
            }
        }

        // 컨텍스트가 없거나 사용자 정보가 없으면 system 반환
        return Optional.of(SYSTEM_USER);
    }
}
