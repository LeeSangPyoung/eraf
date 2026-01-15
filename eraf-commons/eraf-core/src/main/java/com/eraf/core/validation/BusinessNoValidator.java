package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 사업자등록번호 검증 구현 (체크섬 검증)
 */
public class BusinessNoValidator implements ConstraintValidator<BusinessNo, String> {

    // 사업자등록번호 검증을 위한 가중치
    private static final int[] WEIGHTS = {1, 3, 7, 1, 3, 7, 1, 3, 5};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        // 숫자만 추출
        String cleaned = value.replaceAll("[^0-9]", "");

        // 10자리가 아니면 유효하지 않음
        if (cleaned.length() != 10) {
            return false;
        }

        try {
            int sum = 0;

            // 처음 9자리에 대해 가중치를 곱하여 합산
            for (int i = 0; i < 9; i++) {
                sum += (cleaned.charAt(i) - '0') * WEIGHTS[i];
            }

            // 9번째 자리 특별 처리 (5를 곱한 후 10으로 나눈 몫을 더함)
            sum += ((cleaned.charAt(8) - '0') * 5) / 10;

            // 검증 숫자 계산
            int checkDigit = (10 - (sum % 10)) % 10;

            // 마지막 자리와 비교
            return checkDigit == (cleaned.charAt(9) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
