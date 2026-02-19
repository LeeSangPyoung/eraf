package com.eraf.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * ERAF Validation Auto-Configuration
 *
 * <p>커스텀 Bean Validation 애노테이션을 자동 구성합니다.</p>
 *
 * <p>제공하는 Validator:</p>
 * <ul>
 *   <li>@BusinessNo - 사업자등록번호 검증 (체크섬 포함)</li>
 *   <li>@Phone - 한국 전화번호 검증</li>
 *   <li>@Email - 강화된 이메일 검증</li>
 *   <li>@Password - 비밀번호 강도 검증 (대소문자, 숫자, 특수문자)</li>
 *   <li>@NoXss - XSS 공격 패턴 차단</li>
 *   <li>@NoSqlInjection - SQL Injection 차단</li>
 *   <li>@NoPathTraversal - Path Traversal 차단</li>
 *   <li>@FileExtension - 파일 확장자 검증</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * public class CompanyDto {
 *     {@code @BusinessNo}
 *     private String businessNo;  // "1234567890"
 *
 *     {@code @Phone}
 *     private String phone;  // "010-1234-5678"
 *
 *     {@code @Email}
 *     private String email;
 *
 *     {@code @Password}(minLength = 8, requireUpperCase = true, requireNumber = true, requireSpecialChar = true)
 *     private String password;
 *
 *     {@code @NoXss}
 *     private String content;
 *
 *     {@code @FileExtension}(allowed = {"jpg", "png", "pdf"})
 *     private String filename;
 * }
 * </pre>
 *
 * <p>Note: Jakarta Bean Validation 표준을 준수하므로 ConstraintValidator가 자동으로 발견됩니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "jakarta.validation.Validator")
public class ErafValidationAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafValidationAutoConfiguration.class);

    public ErafValidationAutoConfiguration() {
        log.info("ERAF Validation module enabled - Custom validators available");
        log.debug("Available validators: @BusinessNo, @Phone, @Email, @Password, @NoXss, @NoSqlInjection, @NoPathTraversal, @FileExtension");
    }
}
