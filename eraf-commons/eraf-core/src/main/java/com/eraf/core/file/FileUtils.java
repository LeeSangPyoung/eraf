package com.eraf.core.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

/**
 * 파일 유틸리티
 */
public final class FileUtils {

    private static final int BUFFER_SIZE = 8192;

    private FileUtils() {
    }

    /**
     * 파일 복사
     */
    public static void copy(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 파일 복사 (덮어쓰기 옵션)
     */
    public static void copy(Path source, Path target, boolean overwrite) throws IOException {
        if (overwrite) {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, target);
        }
    }

    /**
     * 파일 이동
     */
    public static void move(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 파일 삭제
     */
    public static void delete(Path path) throws IOException {
        Files.deleteIfExists(path);
    }

    /**
     * 디렉토리 및 하위 파일 전체 삭제
     */
    public static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 디렉토리 생성 (상위 디렉토리 포함)
     */
    public static void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    /**
     * 파일 존재 여부
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * 디렉토리 여부
     */
    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * 파일 크기 조회
     */
    public static long size(Path path) throws IOException {
        return Files.size(path);
    }

    /**
     * 파일 확장자 추출
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * 파일명에서 확장자 제거
     */
    public static String getNameWithoutExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0) {
            return filename;
        }
        return filename.substring(0, lastDot);
    }

    /**
     * 파일 읽기 (바이트 배열)
     */
    public static byte[] readBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    /**
     * 파일 쓰기 (바이트 배열)
     */
    public static void writeBytes(Path path, byte[] bytes) throws IOException {
        Files.write(path, bytes);
    }

    /**
     * 파일 읽기 (문자열)
     */
    public static String readString(Path path) throws IOException {
        return Files.readString(path);
    }

    /**
     * 파일 쓰기 (문자열)
     */
    public static void writeString(Path path, String content) throws IOException {
        Files.writeString(path, content);
    }

    /**
     * 스트림 복사
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long count = 0;
        int n;
        while ((n = input.read(buffer)) > -1) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 디렉토리 내 파일 목록 조회
     */
    public static Stream<Path> list(Path dir) throws IOException {
        return Files.list(dir);
    }

    /**
     * 파일 크기 포맷팅 (KB, MB, GB)
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
