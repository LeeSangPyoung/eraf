package com.eraf.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 파일 확장자 검증 구현
 */
public class FileExtensionValidator implements ConstraintValidator<FileExtension, String> {

    private Set<String> allowed;
    private Set<String> denied;
    private boolean caseSensitive;

    @Override
    public void initialize(FileExtension constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();

        this.allowed = Arrays.stream(constraintAnnotation.allowed())
                .map(ext -> caseSensitive ? ext : ext.toLowerCase())
                .collect(Collectors.toSet());

        this.denied = Arrays.stream(constraintAnnotation.denied())
                .map(ext -> caseSensitive ? ext : ext.toLowerCase())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String extension = extractExtension(value);
        if (extension == null) {
            return true; // 확장자가 없으면 검사 패스
        }

        if (!caseSensitive) {
            extension = extension.toLowerCase();
        }

        // 허용 목록이 있으면 허용 목록에 있어야 함
        if (!allowed.isEmpty()) {
            return allowed.contains(extension);
        }

        // 금지 목록에 없어야 함
        return !denied.contains(extension);
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1);
    }
}
