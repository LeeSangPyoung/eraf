package com.eraf.core.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileValidationUtilsTest {

    @Test
    @DisplayName("이미지 파일 확장자 검증 - 허용")
    void testIsImageFileAllowed() {
        // Given
        MockMultipartFile jpgFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile pngFile = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());
        MockMultipartFile gifFile = new MockMultipartFile("file", "test.gif", "image/gif", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isImageFile(jpgFile));
        assertTrue(FileValidationUtils.isImageFile(pngFile));
        assertTrue(FileValidationUtils.isImageFile(gifFile));
    }

    @Test
    @DisplayName("이미지 파일 확장자 검증 - 거부")
    void testIsImageFileRejected() {
        // Given
        MockMultipartFile txtFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        MockMultipartFile exeFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", "content".getBytes());

        // Then
        assertFalse(FileValidationUtils.isImageFile(txtFile));
        assertFalse(FileValidationUtils.isImageFile(exeFile));
    }

    @Test
    @DisplayName("문서 파일 확장자 검증")
    void testIsDocumentFile() {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile docxFile = new MockMultipartFile("file", "test.docx", "application/vnd.openxmlformats", "content".getBytes());
        MockMultipartFile xlsxFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isDocumentFile(pdfFile));
        assertTrue(FileValidationUtils.isDocumentFile(docxFile));
        assertTrue(FileValidationUtils.isDocumentFile(xlsxFile));
    }

    @Test
    @DisplayName("위험한 파일 확장자 검증")
    void testIsDangerousFile() {
        // Given
        MockMultipartFile exeFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", "content".getBytes());
        MockMultipartFile batFile = new MockMultipartFile("file", "test.bat", "text/plain", "content".getBytes());
        MockMultipartFile shFile = new MockMultipartFile("file", "test.sh", "text/plain", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isDangerousFile(exeFile));
        assertTrue(FileValidationUtils.isDangerousFile(batFile));
        assertTrue(FileValidationUtils.isDangerousFile(shFile));
    }

    @Test
    @DisplayName("안전한 파일은 위험하지 않음")
    void testSafeFileNotDangerous() {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile jpgFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        // Then
        assertFalse(FileValidationUtils.isDangerousFile(pdfFile));
        assertFalse(FileValidationUtils.isDangerousFile(jpgFile));
    }

    @Test
    @DisplayName("파일 크기 검증 - 바이트 단위")
    void testIsValidSize() {
        // Given
        byte[] content = new byte[1000];
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);

        // Then
        assertTrue(FileValidationUtils.isValidSize(file, 2000)); // 크기 내
        assertTrue(FileValidationUtils.isValidSize(file, 1000)); // 정확히 같음
        assertFalse(FileValidationUtils.isValidSize(file, 500)); // 초과
    }

    @Test
    @DisplayName("파일 크기 검증 - MB 단위")
    void testIsValidSizeMB() {
        // Given
        byte[] content = new byte[500 * 1024]; // 500KB
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", content);

        // Then
        assertTrue(FileValidationUtils.isValidSizeMB(file, 1)); // 1MB 이내
        assertFalse(FileValidationUtils.isValidSizeMB(file, 0)); // 0MB 초과
    }

    @Test
    @DisplayName("빈 파일 확인")
    void testIsEmpty() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        MockMultipartFile normalFile = new MockMultipartFile("file", "normal.txt", "text/plain", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isEmpty(emptyFile));
        assertTrue(FileValidationUtils.isEmpty(null));
        assertFalse(FileValidationUtils.isEmpty(normalFile));
    }

    @Test
    @DisplayName("커스텀 확장자 검증")
    void testIsAllowedExtension() {
        // Given
        Set<String> allowedExtensions = Set.of("csv", "json");
        MockMultipartFile csvFile = new MockMultipartFile("file", "data.csv", "text/csv", "content".getBytes());
        MockMultipartFile jsonFile = new MockMultipartFile("file", "data.json", "application/json", "content".getBytes());
        MockMultipartFile xmlFile = new MockMultipartFile("file", "data.xml", "application/xml", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isAllowedExtension(csvFile, allowedExtensions));
        assertTrue(FileValidationUtils.isAllowedExtension(jsonFile, allowedExtensions));
        assertFalse(FileValidationUtils.isAllowedExtension(xmlFile, allowedExtensions));
    }

    @Test
    @DisplayName("종합 검증 - 성공")
    void testValidateSuccess() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        // When
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(file, FileValidationUtils.DEFAULT_DOCUMENT_EXTENSIONS, 10);

        // Then
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    @DisplayName("종합 검증 - 빈 파일 실패")
    void testValidateEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        // When
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(emptyFile, FileValidationUtils.DEFAULT_DOCUMENT_EXTENSIONS, 10);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("비어있습니다"));
    }

    @Test
    @DisplayName("종합 검증 - 위험한 파일 실패")
    void testValidateDangerousFile() {
        // Given
        MockMultipartFile exeFile = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "content".getBytes());

        // When
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(exeFile, Set.of("exe"), 10);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("허용되지 않는 파일 형식"));
    }

    @Test
    @DisplayName("종합 검증 - 확장자 불허 실패")
    void testValidateInvalidExtension() {
        // Given
        MockMultipartFile xmlFile = new MockMultipartFile("file", "data.xml", "application/xml", "content".getBytes());

        // When
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(xmlFile, Set.of("json"), 10);

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("확장자"));
    }

    @Test
    @DisplayName("종합 검증 - 크기 초과 실패")
    void testValidateSizeExceeded() {
        // Given
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        MockMultipartFile largeFile = new MockMultipartFile("file", "large.pdf", "application/pdf", largeContent);

        // When
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(largeFile, FileValidationUtils.DEFAULT_DOCUMENT_EXTENSIONS, 1); // 1MB 제한

        // Then
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("크기"));
    }

    @Test
    @DisplayName("이미지 유효성 검증")
    void testValidateImage() {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        // When
        FileValidationUtils.ValidationResult result = FileValidationUtils.validateImage(imageFile, 10);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("문서 유효성 검증")
    void testValidateDocument() {
        // Given
        MockMultipartFile docFile = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());

        // When
        FileValidationUtils.ValidationResult result = FileValidationUtils.validateDocument(docFile, 10);

        // Then
        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("ValidationResult.throwIfInvalid() 예외 발생")
    void testThrowIfInvalid() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(emptyFile, Set.of("txt"), 10);

        // Then
        assertThrows(FileStorageException.class, result::throwIfInvalid);
    }

    @Test
    @DisplayName("ValidationResult.throwIfInvalid() 성공 시 예외 없음")
    void testThrowIfInvalidSuccess() {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        FileValidationUtils.ValidationResult result =
                FileValidationUtils.validate(file, Set.of("txt"), 10);

        // Then - 예외 없이 통과
        assertDoesNotThrow(result::throwIfInvalid);
    }

    @Test
    @DisplayName("확장자 없는 파일명")
    void testFileWithoutExtension() {
        // Given
        MockMultipartFile noExtFile = new MockMultipartFile("file", "noextension", "text/plain", "content".getBytes());

        // Then
        assertFalse(FileValidationUtils.isImageFile(noExtFile));
        assertFalse(FileValidationUtils.isDocumentFile(noExtFile));
        assertFalse(FileValidationUtils.isDangerousFile(noExtFile));
    }

    @Test
    @DisplayName("대소문자 무시 확장자 검증")
    void testCaseInsensitiveExtension() {
        // Given
        MockMultipartFile upperCaseFile = new MockMultipartFile("file", "test.JPG", "image/jpeg", "content".getBytes());
        MockMultipartFile mixedCaseFile = new MockMultipartFile("file", "test.PnG", "image/png", "content".getBytes());

        // Then
        assertTrue(FileValidationUtils.isImageFile(upperCaseFile));
        assertTrue(FileValidationUtils.isImageFile(mixedCaseFile));
    }
}
