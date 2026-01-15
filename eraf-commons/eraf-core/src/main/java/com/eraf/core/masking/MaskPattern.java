package com.eraf.core.masking;

/**
 * 마스킹 패턴
 */
public enum MaskPattern {

    /**
     * 기본 패턴 (각 타입별 기본)
     */
    DEFAULT,

    /**
     * 앞 N자리만 표시
     */
    SHOW_FIRST,

    /**
     * 뒤 N자리만 표시
     */
    SHOW_LAST,

    /**
     * 중간만 마스킹
     */
    MASK_MIDDLE,

    /**
     * 전체 마스킹
     */
    MASK_ALL
}
