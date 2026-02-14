package com.eraf.jpa.encryption;

import jakarta.persistence.Convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 민감 컬럼 자동 암복호화 어노테이션
 * JPA AttributeConverter를 통해 DB 저장 시 자동 암호화, 조회 시 자동 복호화
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @Entity}
 * public class User {
 *     {@code @Encrypt}
 *     {@code @Column(length = 1000)}  // 암호화 시 길이가 증가하므로 충분한 크기 설정
 *     private String socialSecurityNumber;  // 주민번호 자동 암복호화
 *
 *     {@code @Encrypt}
 *     private String creditCardNumber;  // 카드번호 자동 암복호화
 * }
 * </pre>
 *
 * <p>주의사항:</p>
 * <ul>
 *     <li>암호화된 데이터는 원본보다 길이가 증가하므로 컬럼 길이를 충분히 설정해야 함</li>
 *     <li>암호화된 컬럼은 DB에서 직접 검색/정렬이 불가능함 (애플리케이션 레벨에서 처리 필요)</li>
 *     <li>암호화 키가 변경되면 기존 데이터 복호화 불가 (키 관리 중요)</li>
 * </ul>
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Convert(converter = EncryptedStringConverter.class)
public @interface Encrypt {
}
