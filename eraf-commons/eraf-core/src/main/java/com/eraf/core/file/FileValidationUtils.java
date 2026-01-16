package com.eraf.core.file;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 파일 유효성 검사 유틸리티
 */
public class FileValidationUtils {

    /**
     * 기본 허용 이미지 확장자
     */
    public static final Set<String> DEFAULT_IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")
    );

    /**
     * 기본 허용 문서 확장자
     */
    public static final Set<String> DEFAULT_DOCUMENT_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "txt", "csv")
    );

    /**
     * 위험한 확장자 목록
     */
    public static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(
            Arrays.asList("exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "jar", "msi", "dll",
                    "scr", "com", "pif", "hta", "cpl", "msc", "inf", "reg", "ws", "wsf", "wsh")
    );

    private FileValidationUtils() {
    }

    /**
     * 파일 확장자 검증
     *
     * @param file              파일
     * @param allowedExtensions 허용 확장자
     * @return 유효 여부
     */
    public static boolean isAllowedExtension(MultipartFile file, Set<String> allowedExtensions) {
        String extension = getExtension(file.getOriginalFilename());
        return extension != null && allowedExtensions.contains(extension.toLowerCase());
    }

    /**
     * 이미지 파일 여부 검증
     */
    public static boolean isImageFile(MultipartFile file) {
        return isAllowedExtension(file, DEFAULT_IMAGE_EXTENSIONS);
    }

    /**
     * 문서 파일 여부 검증
     */
    public static boolean isDocumentFile(MultipartFile file) {
        return isAllowedExtension(file, DEFAULT_DOCUMENT_EXTENSIONS);
    }

    /**
     * 위험한 파일 여부 검증
     */
    public static boolean isDangerousFile(MultipartFile file) {
        String extension = getExtension(file.getOriginalFilename());
        return extension != null && DANGEROUS_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 파일 크기 검증
     *
     * @param file    파일
     * @param maxSize 최대 크기 (bytes)
     * @return 유효 여부
     */
    public static boolean isValidSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }

    /**
     * 파일 크기 검증 (MB 단위)
     *
     * @param file     파일
     * @param maxSizeMB 최대 크기 (MB)
     * @return 유효 여부
     */
    public static boolean isValidSizeMB(MultipartFile file, int maxSizeMB) {
        return isValidSize(file, maxSizeMB * 1024L * 1024L);
    }

    /**
     * 빈 파일 여부 확인
     */
    public static boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty() || file.getSize() == 0;
    }

    /**
     * 파일 유효성 검증 (종합)
     *
     * @param file              파일
     * @param allowedExtensions 허용 확장자
     * @param maxSizeMB         최대 크기 (MB)
     * @return 검증 결과
     */
    public static ValidationResult validate(MultipartFile file, Set<String> allowedExtensions, int maxSizeMB) {
        if (isEmpty(file)) {
            return ValidationResult.fail("파일이 비어있습니다.");
        }

        if (isDangerousFile(file)) {
            return ValidationResult.fail("허용되지 않는 파일 형식입니다.");
        }

        if (!isAllowedExtension(file, allowedExtensions)) {
            return ValidationResult.fail("허용되지 않는 확장자입니다. 허용 확장자: " + allowedExtensions);
        }

        if (!isValidSizeMB(file, maxSizeMB)) {
            return ValidationResult.fail("파일 크기가 제한을 초과했습니다. 최대 " + maxSizeMB + "MB");
        }

        return ValidationResult.success();
    }

    /**
     * 이미지 파일 유효성 검증
     *
     * @param file      파일
     * @param maxSizeMB 최대 크기 (MB)
     * @return 검증 결과
     */
    public static ValidationResult validateImage(MultipartFile file, int maxSizeMB) {
        return validate(file, DEFAULT_IMAGE_EXTENSIONS, maxSizeMB);
    }

    /**
     * 문서 파일 유효성 검증
     *
     * @param file      파일
     * @param maxSizeMB 최대 크기 (MB)
     * @return 검증 결과
     */
    public static ValidationResult validateDocument(MultipartFile file, int maxSizeMB) {
        return validate(file, DEFAULT_DOCUMENT_EXTENSIONS, maxSizeMB);
    }

    /**
     * 확장자 추출
     */
    private static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex + 1).toLowerCase() : null;
    }

    /**
     * 검증 결과
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public void throwIfInvalid() {
            if (!valid) {
                throw new FileStorageException(message);
            }
        }
    }
}
