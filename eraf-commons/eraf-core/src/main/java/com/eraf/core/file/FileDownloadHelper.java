package com.eraf.core.file;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 파일 다운로드 헬퍼
 * 브라우저 호환 파일 다운로드 응답 생성
 */
public class FileDownloadHelper {

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        // 문서
        CONTENT_TYPE_MAP.put("pdf", "application/pdf");
        CONTENT_TYPE_MAP.put("doc", "application/msword");
        CONTENT_TYPE_MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        CONTENT_TYPE_MAP.put("xls", "application/vnd.ms-excel");
        CONTENT_TYPE_MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        CONTENT_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        CONTENT_TYPE_MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        CONTENT_TYPE_MAP.put("hwp", "application/x-hwp");
        CONTENT_TYPE_MAP.put("txt", "text/plain");
        CONTENT_TYPE_MAP.put("csv", "text/csv");
        CONTENT_TYPE_MAP.put("rtf", "application/rtf");

        // 이미지
        CONTENT_TYPE_MAP.put("jpg", "image/jpeg");
        CONTENT_TYPE_MAP.put("jpeg", "image/jpeg");
        CONTENT_TYPE_MAP.put("png", "image/png");
        CONTENT_TYPE_MAP.put("gif", "image/gif");
        CONTENT_TYPE_MAP.put("bmp", "image/bmp");
        CONTENT_TYPE_MAP.put("webp", "image/webp");
        CONTENT_TYPE_MAP.put("svg", "image/svg+xml");
        CONTENT_TYPE_MAP.put("ico", "image/x-icon");

        // 압축
        CONTENT_TYPE_MAP.put("zip", "application/zip");
        CONTENT_TYPE_MAP.put("rar", "application/x-rar-compressed");
        CONTENT_TYPE_MAP.put("7z", "application/x-7z-compressed");
        CONTENT_TYPE_MAP.put("tar", "application/x-tar");
        CONTENT_TYPE_MAP.put("gz", "application/gzip");

        // 비디오
        CONTENT_TYPE_MAP.put("mp4", "video/mp4");
        CONTENT_TYPE_MAP.put("avi", "video/x-msvideo");
        CONTENT_TYPE_MAP.put("mov", "video/quicktime");
        CONTENT_TYPE_MAP.put("wmv", "video/x-ms-wmv");
        CONTENT_TYPE_MAP.put("webm", "video/webm");

        // 오디오
        CONTENT_TYPE_MAP.put("mp3", "audio/mpeg");
        CONTENT_TYPE_MAP.put("wav", "audio/wav");
        CONTENT_TYPE_MAP.put("ogg", "audio/ogg");
        CONTENT_TYPE_MAP.put("flac", "audio/flac");

        // 기타
        CONTENT_TYPE_MAP.put("json", "application/json");
        CONTENT_TYPE_MAP.put("xml", "application/xml");
        CONTENT_TYPE_MAP.put("html", "text/html");
        CONTENT_TYPE_MAP.put("js", "application/javascript");
        CONTENT_TYPE_MAP.put("css", "text/css");
    }

    private FileDownloadHelper() {
    }

    /**
     * 파일 다운로드 응답 생성
     *
     * @param resource Resource
     * @param filename 다운로드 파일명
     * @return ResponseEntity
     */
    public static ResponseEntity<Resource> download(Resource resource, String filename) {
        String contentType = getContentType(filename);
        return download(resource, filename, contentType);
    }

    /**
     * 파일 다운로드 응답 생성 (Content-Type 지정)
     *
     * @param resource    Resource
     * @param filename    다운로드 파일명
     * @param contentType Content-Type
     * @return ResponseEntity
     */
    public static ResponseEntity<Resource> download(Resource resource, String filename, String contentType) {
        String encodedFilename = encodeFilename(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    /**
     * 인라인 표시 응답 생성 (브라우저에서 직접 표시)
     *
     * @param resource Resource
     * @param filename 파일명
     * @return ResponseEntity
     */
    public static ResponseEntity<Resource> inline(Resource resource, String filename) {
        String contentType = getContentType(filename);
        String encodedFilename = encodeFilename(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    /**
     * 파일명 URL 인코딩
     */
    public static String encodeFilename(String filename) {
        try {
            return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return filename;
        }
    }

    /**
     * 확장자로 Content-Type 추출
     */
    public static String getContentType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            String extension = filename.substring(dotIndex + 1).toLowerCase();
            return CONTENT_TYPE_MAP.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    /**
     * 이미지 파일 여부 확인
     */
    public static boolean isImage(String filename) {
        String contentType = getContentType(filename);
        return contentType.startsWith("image/");
    }

    /**
     * 비디오 파일 여부 확인
     */
    public static boolean isVideo(String filename) {
        String contentType = getContentType(filename);
        return contentType.startsWith("video/");
    }

    /**
     * 오디오 파일 여부 확인
     */
    public static boolean isAudio(String filename) {
        String contentType = getContentType(filename);
        return contentType.startsWith("audio/");
    }

    /**
     * 문서 파일 여부 확인
     */
    public static boolean isDocument(String filename) {
        String extension = getExtension(filename);
        return extension != null && (
                extension.equals("pdf") ||
                extension.equals("doc") || extension.equals("docx") ||
                extension.equals("xls") || extension.equals("xlsx") ||
                extension.equals("ppt") || extension.equals("pptx") ||
                extension.equals("hwp") || extension.equals("txt") ||
                extension.equals("csv") || extension.equals("rtf")
        );
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
}
