package com.eraf.starter.database.audit;

import com.eraf.core.context.ErafContext;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * ERAF Auditor Aware
 * ErafContext에서 현재 사용자 정보를 가져와 Auditing에 사용
 */
public class ErafAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String userId = ErafContext.getCurrentUserId();
        if (userId != null) {
            return Optional.of(userId);
        }
        return Optional.of("system");
    }
}
