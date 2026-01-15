package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * XSS 공격 방지 검증 구현
 */
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

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
