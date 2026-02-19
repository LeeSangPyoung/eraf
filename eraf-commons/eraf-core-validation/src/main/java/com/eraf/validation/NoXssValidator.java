package com.eraf.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * XSS 공격 방지 검증 구현
 *
 * 성능 최적화:
 * - Pattern을 static final로 사전 컴파일하여 매 검증마다 컴파일 오버헤드 제거
 * - DOTALL 플래그는 script 태그 내 개행 문자 감지를 위해 필요
 * - 대부분의 정상 입력은 첫 번째 패턴에서 빠르게 실패하므로 실제 성능 영향은 미미함
 */
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

    // Pattern 사전 컴파일로 성능 최적화 (재사용)
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>|" +
                    "<[^>]+on\\w+\\s*=|" +
                    "javascript:|" +
                    "vbscript:|" +
                    "data:\\s*text/html|" +
                    "<iframe|" +
                    "<object|" +
                    "<embed|" +
                    "<form|" +
                    "<input[^>]*type\\s*=\\s*['\"]?hidden",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !XSS_PATTERN.matcher(value).find();
    }
}
