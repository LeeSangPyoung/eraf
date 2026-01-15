package com.eraf.core.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일 타입 감지기 (매직 넘버 기반)
 */
public final class FileTypeDetector {

    private static final Map<String, String> MAGIC_NUMBERS = new HashMap<>();
    private static final Map<String, String> EXTENSION_MIMES = new HashMap<>();

    static {
        // 매직 넘버 정의 (Hex)
        MAGIC_NUMBERS.put("89504E47", "image/png");
        MAGIC_NUMBERS.put("FFD8FF", "image/jpeg");
        MAGIC_NUMBERS.put("47494638", "image/gif");
        MAGIC_NUMBERS.put("424D", "image/bmp");
        MAGIC_NUMBERS.put("52494646", "image/webp"); // RIFF
        MAGIC_NUMBERS.put("25504446", "application/pdf");
        MAGIC_NUMBERS.put("504B0304", "application/zip");
        MAGIC_NUMBERS.put("504B0506", "application/zip");
        MAGIC_NUMBERS.put("504B0708", "application/zip");
        MAGIC_NUMBERS.put("1F8B08", "application/gzip");
        MAGIC_NUMBERS.put("377ABCAF", "application/x-7z-compressed");
        MAGIC_NUMBERS.put("526172211A07", "application/x-rar-compressed");

        // 확장자 기반 MIME
        EXTENSION_MIMES.put("txt", "text/plain");
        EXTENSION_MIMES.put("html", "text/html");
        EXTENSION_MIMES.put("htm", "text/html");
        EXTENSION_MIMES.put("css", "text/css");
        EXTENSION_MIMES.put("js", "application/javascript");
        EXTENSION_MIMES.put("json", "application/json");
        EXTENSION_MIMES.put("xml", "application/xml");
        EXTENSION_MIMES.put("csv", "text/csv");
        EXTENSION_MIMES.put("png", "image/png");
        EXTENSION_MIMES.put("jpg", "image/jpeg");
        EXTENSION_MIMES.put("jpeg", "image/jpeg");
        EXTENSION_MIMES.put("gif", "image/gif");
        EXTENSION_MIMES.put("bmp", "image/bmp");
        EXTENSION_MIMES.put("webp", "image/webp");
        EXTENSION_MIMES.put("svg", "image/svg+xml");
        EXTENSION_MIMES.put("ico", "image/x-icon");
        EXTENSION_MIMES.put("pdf", "application/pdf");
        EXTENSION_MIMES.put("zip", "application/zip");
        EXTENSION_MIMES.put("gz", "application/gzip");
        EXTENSION_MIMES.put("tar", "application/x-tar");
        EXTENSION_MIMES.put("rar", "application/x-rar-compressed");
        EXTENSION_MIMES.put("7z", "application/x-7z-compressed");
        EXTENSION_MIMES.put("doc", "application/msword");
        EXTENSION_MIMES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_MIMES.put("xls", "application/vnd.ms-excel");
        EXTENSION_MIMES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_MIMES.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_MIMES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXTENSION_MIMES.put("mp3", "audio/mpeg");
        EXTENSION_MIMES.put("mp4", "video/mp4");
        EXTENSION_MIMES.put("avi", "video/x-msvideo");
        EXTENSION_MIMES.put("mov", "video/quicktime");
    }

    private FileTypeDetector() {
    }

    /**
     * 매직 넘버로 실제 파일 타입 감지
     */
    public static String detectByContent(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return detectByContent(is);
        }
    }

    /**
     * 매직 넘버로 실제 파일 타입 감지
     */
    public static String detectByContent(InputStream inputStream) throws IOException {
        byte[] header = new byte[12];
        int read = inputStream.read(header);
        if (read < 2) {
            return "application/octet-stream";
        }

        String hex = bytesToHex(header, read);

        for (Map.Entry<String, String> entry : MAGIC_NUMBERS.entrySet()) {
            if (hex.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "application/octet-stream";
    }

    /**
     * 확장자로 MIME 타입 추정
     */
    public static String detectByExtension(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String extension = FileUtils.getExtension(filename);
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }
        return EXTENSION_MIMES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    /**
     * 파일 타입 검증 (확장자와 실제 내용 일치 여부)
     */
    public static boolean validateType(Path path) throws IOException {
        String filename = path.getFileName().toString();
        String extensionMime = detectByExtension(filename);
        String actualMime = detectByContent(path);

        // 확장자 기반 MIME과 실제 MIME 비교
        if (extensionMime.equals("application/octet-stream")) {
            return true; // 알 수 없는 확장자는 검증 통과
        }

        return extensionMime.equals(actualMime) ||
                (extensionMime.startsWith("image/") && actualMime.startsWith("image/")) ||
                (extensionMime.equals("application/zip") && actualMime.equals("application/zip"));
    }

    /**
     * 이미지 파일 여부 확인
     */
    public static boolean isImage(Path path) throws IOException {
        String mime = detectByContent(path);
        return mime.startsWith("image/");
    }

    /**
     * PDF 파일 여부 확인
     */
    public static boolean isPdf(Path path) throws IOException {
        String mime = detectByContent(path);
        return "application/pdf".equals(mime);
    }

    /**
     * 압축 파일 여부 확인
     */
    public static boolean isArchive(Path path) throws IOException {
        String mime = detectByContent(path);
        return mime.equals("application/zip") ||
                mime.equals("application/gzip") ||
                mime.equals("application/x-7z-compressed") ||
                mime.equals("application/x-rar-compressed");
    }

    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }
}
