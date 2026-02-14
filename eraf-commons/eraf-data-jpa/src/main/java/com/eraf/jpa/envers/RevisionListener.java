package com.eraf.jpa.envers;

import com.eraf.core.context.ErafContext;
import com.eraf.core.context.ErafContextHolder;
import org.hibernate.envers.RevisionType;

/**
 * Hibernate Envers Revision Listener
 * 엔티티 변경 시 사용자 정보 등을 자동으로 기록
 */
public class RevisionListener implements org.hibernate.envers.RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        if (revisionEntity instanceof RevisionEntity) {
            RevisionEntity revision = (RevisionEntity) revisionEntity;

            // ErafContext에서 사용자 정보 가져오기
            ErafContext context = ErafContextHolder.getContext();
            if (context != null) {
                revision.setUserId(context.getUserId());
                revision.setUsername(context.getUsername());
                revision.setClientIp(context.getClientIp());
            }

            // 기본값 설정
            if (revision.getUserId() == null) {
                revision.setUserId("system");
            }
            if (revision.getUsername() == null) {
                revision.setUsername("system");
            }
        }
    }

    /**
     * Revision Type을 문자열로 변환
     */
    public static String getRevisionTypeString(RevisionType revisionType) {
        if (revisionType == null) {
            return "UNKNOWN";
        }
        switch (revisionType) {
            case ADD:
                return "INSERT";
            case MOD:
                return "UPDATE";
            case DEL:
                return "DELETE";
            default:
                return "UNKNOWN";
        }
    }
}
