package com.eraf.core.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * I/O 스트림 유틸리티
 * 파일 읽기/쓰기, 스트림 처리, 리소스 복사 등
 */
public final class IoUtils {

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final int EOF = -1;

    private IoUtils() {
    }

    // ===== InputStream → String =====

    /**
     * InputStream을 문자열로 변환 (UTF-8)
     */
    public static String toString(InputStream input) throws IOException {
        return toString(input, StandardCharsets.UTF_8);
    }

    /**
     * InputStream을 문자열로 변환 (Charset 지정)
     */
    public static String toString(InputStream input, Charset charset) throws IOException {
        if (input == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        // 마지막 줄바꿈 제거
        if (sb.length() > 0) {
            sb.setLength(sb.length() - System.lineSeparator().length());
        }
        return sb.toString();
    }

    /**
     * InputStream을 문자열로 변환 (예외 무시)
     */
    public static String toStringQuietly(InputStream input) {
        try {
            return toString(input);
        } catch (IOException e) {
            return null;
        }
    }

    // ===== InputStream → byte[] =====

    /**
     * InputStream을 바이트 배열로 변환
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * InputStream을 바이트 배열로 변환 (예외 무시)
     */
    public static byte[] toByteArrayQuietly(InputStream input) {
        try {
            return toByteArray(input);
        } catch (IOException e) {
            return null;
        }
    }

    // ===== InputStream → List<String> =====

    /**
     * InputStream을 줄 단위 리스트로 변환 (UTF-8)
     */
    public static List<String> readLines(InputStream input) throws IOException {
        return readLines(input, StandardCharsets.UTF_8);
    }

    /**
     * InputStream을 줄 단위 리스트로 변환 (Charset 지정)
     */
    public static List<String> readLines(InputStream input, Charset charset) throws IOException {
        if (input == null) {
            return new ArrayList<>();
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    // ===== Reader → String =====

    /**
     * Reader를 문자열로 변환
     */
    public static String toString(Reader reader) throws IOException {
        if (reader == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while (EOF != (n = reader.read(buffer))) {
            sb.append(buffer, 0, n);
        }
        return sb.toString();
    }

    // ===== 복사 (Copy) =====

    /**
     * InputStream을 OutputStream으로 복사
     * @return 복사된 바이트 수
     */
    public static long copy(InputStream input, OutputStream output) throws IOException {
        if (input == null || output == null) {
            return 0;
        }
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * InputStream을 OutputStream으로 복사 (예외 무시)
     */
    public static long copyQuietly(InputStream input, OutputStream output) {
        try {
            return copy(input, output);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Reader를 Writer로 복사
     * @return 복사된 문자 수
     */
    public static long copy(Reader reader, Writer writer) throws IOException {
        if (reader == null || writer == null) {
            return 0;
        }
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    // ===== 파일 읽기 =====

    /**
     * 파일을 문자열로 읽기 (UTF-8)
     */
    public static String readFileToString(File file) throws IOException {
        return readFileToString(file, StandardCharsets.UTF_8);
    }

    /**
     * 파일을 문자열로 읽기 (Charset 지정)
     */
    public static String readFileToString(File file, Charset charset) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        try (InputStream input = new FileInputStream(file)) {
            return toString(input, charset);
        }
    }

    /**
     * 파일을 바이트 배열로 읽기
     */
    public static byte[] readFileToByteArray(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }
        try (InputStream input = new FileInputStream(file)) {
            return toByteArray(input);
        }
    }

    /**
     * 파일을 줄 단위 리스트로 읽기 (UTF-8)
     */
    public static List<String> readLines(File file) throws IOException {
        return readLines(file, StandardCharsets.UTF_8);
    }

    /**
     * 파일을 줄 단위 리스트로 읽기 (Charset 지정)
     */
    public static List<String> readLines(File file, Charset charset) throws IOException {
        if (file == null || !file.exists()) {
            return new ArrayList<>();
        }
        try (InputStream input = new FileInputStream(file)) {
            return readLines(input, charset);
        }
    }

    // ===== 파일 쓰기 =====

    /**
     * 문자열을 파일에 쓰기 (UTF-8, 덮어쓰기)
     */
    public static void writeStringToFile(File file, String data) throws IOException {
        writeStringToFile(file, data, StandardCharsets.UTF_8, false);
    }

    /**
     * 문자열을 파일에 쓰기 (Charset 지정, 덮어쓰기)
     */
    public static void writeStringToFile(File file, String data, Charset charset) throws IOException {
        writeStringToFile(file, data, charset, false);
    }

    /**
     * 문자열을 파일에 쓰기 (UTF-8, append 옵션)
     */
    public static void writeStringToFile(File file, String data, boolean append) throws IOException {
        writeStringToFile(file, data, StandardCharsets.UTF_8, append);
    }

    /**
     * 문자열을 파일에 쓰기 (Charset 지정, append 옵션)
     */
    public static void writeStringToFile(File file, String data, Charset charset, boolean append) throws IOException {
        if (file == null || data == null) {
            return;
        }
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file, append), charset)) {
            writer.write(data);
        }
    }

    /**
     * 바이트 배열을 파일에 쓰기 (덮어쓰기)
     */
    public static void writeByteArrayToFile(File file, byte[] data) throws IOException {
        writeByteArrayToFile(file, data, false);
    }

    /**
     * 바이트 배열을 파일에 쓰기 (append 옵션)
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        if (file == null || data == null) {
            return;
        }
        try (OutputStream output = new FileOutputStream(file, append)) {
            output.write(data);
        }
    }

    /**
     * 줄 단위 리스트를 파일에 쓰기 (UTF-8, 덮어쓰기)
     */
    public static void writeLines(File file, List<String> lines) throws IOException {
        writeLines(file, lines, StandardCharsets.UTF_8, false);
    }

    /**
     * 줄 단위 리스트를 파일에 쓰기 (Charset 지정, append 옵션)
     */
    public static void writeLines(File file, List<String> lines, Charset charset, boolean append) throws IOException {
        if (file == null || lines == null) {
            return;
        }
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file, append), charset)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
    }

    // ===== 파일 복사/이동 =====

    /**
     * 파일 복사
     */
    public static void copyFile(File source, File dest) throws IOException {
        if (source == null || dest == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }
        if (!source.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + source);
        }
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 파일 이동
     */
    public static void moveFile(File source, File dest) throws IOException {
        if (source == null || dest == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }
        if (!source.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + source);
        }
        Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 디렉토리 복사 (재귀적)
     */
    public static void copyDirectory(File sourceDir, File destDir) throws IOException {
        if (sourceDir == null || destDir == null) {
            throw new IllegalArgumentException("Source and destination must not be null");
        }
        if (!sourceDir.exists()) {
            throw new FileNotFoundException("Source directory does not exist: " + sourceDir);
        }
        if (!sourceDir.isDirectory()) {
            throw new IllegalArgumentException("Source is not a directory: " + sourceDir);
        }

        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(destDir, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }

    // ===== 리소스 닫기 =====

    /**
     * Closeable 리소스 닫기 (예외 무시)
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // 무시
            }
        }
    }

    /**
     * 여러 Closeable 리소스 닫기 (예외 무시)
     */
    public static void closeQuietly(Closeable... closeables) {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                closeQuietly(closeable);
            }
        }
    }

    // ===== 스트림 생성 =====

    /**
     * 문자열을 InputStream으로 변환 (UTF-8)
     */
    public static InputStream toInputStream(String str) {
        return toInputStream(str, StandardCharsets.UTF_8);
    }

    /**
     * 문자열을 InputStream으로 변환 (Charset 지정)
     */
    public static InputStream toInputStream(String str, Charset charset) {
        if (str == null) {
            return null;
        }
        return new ByteArrayInputStream(str.getBytes(charset));
    }

    /**
     * 바이트 배열을 InputStream으로 변환
     */
    public static InputStream toInputStream(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    // ===== 파일 정보 =====

    /**
     * 파일 크기 (바이트)
     */
    public static long sizeOf(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        if (file.isDirectory()) {
            return sizeOfDirectory(file);
        }
        return file.length();
    }

    /**
     * 디렉토리 크기 (바이트, 재귀적)
     */
    public static long sizeOfDirectory(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += sizeOfDirectory(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    // ===== 파일/디렉토리 삭제 =====

    /**
     * 파일 삭제
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return file.delete();
    }

    /**
     * 디렉토리 삭제 (재귀적, 내부 파일 모두 삭제)
     */
    public static boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return false;
        }
        if (!directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }

    /**
     * 파일 또는 디렉토리 삭제 (예외 무시)
     */
    public static boolean deleteQuietly(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                return deleteDirectory(file);
            } else {
                return file.delete();
            }
        } catch (Exception e) {
            return false;
        }
    }

    // ===== 디렉토리 생성 =====

    /**
     * 디렉토리 생성 (부모 디렉토리도 생성)
     */
    public static boolean createDirectory(File directory) {
        if (directory == null) {
            return false;
        }
        if (directory.exists()) {
            return directory.isDirectory();
        }
        return directory.mkdirs();
    }

    /**
     * 파일의 부모 디렉토리 생성
     */
    public static boolean createParentDirectories(File file) {
        if (file == null) {
            return false;
        }
        File parent = file.getParentFile();
        if (parent == null) {
            return true;
        }
        return createDirectory(parent);
    }

    // ===== 버퍼 처리 =====

    /**
     * InputStream을 BufferedInputStream으로 변환
     */
    public static BufferedInputStream toBufferedInputStream(InputStream input) {
        if (input == null) {
            return null;
        }
        if (input instanceof BufferedInputStream) {
            return (BufferedInputStream) input;
        }
        return new BufferedInputStream(input);
    }

    /**
     * OutputStream을 BufferedOutputStream으로 변환
     */
    public static BufferedOutputStream toBufferedOutputStream(OutputStream output) {
        if (output == null) {
            return null;
        }
        if (output instanceof BufferedOutputStream) {
            return (BufferedOutputStream) output;
        }
        return new BufferedOutputStream(output);
    }

    /**
     * Reader를 BufferedReader로 변환
     */
    public static BufferedReader toBufferedReader(Reader reader) {
        if (reader == null) {
            return null;
        }
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }
        return new BufferedReader(reader);
    }

    /**
     * Writer를 BufferedWriter로 변환
     */
    public static BufferedWriter toBufferedWriter(Writer writer) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof BufferedWriter) {
            return (BufferedWriter) writer;
        }
        return new BufferedWriter(writer);
    }

    // ===== 파일 확장자 =====

    /**
     * 파일 확장자 추출
     * 예: "test.txt" -> "txt"
     */
    public static String getExtension(File file) {
        if (file == null) {
            return null;
        }
        return getExtension(file.getName());
    }

    /**
     * 파일명에서 확장자 추출
     * 예: "test.txt" -> "txt"
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDot + 1);
    }

    /**
     * 확장자 제거
     * 예: "test.txt" -> "test"
     */
    public static String removeExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return filename;
        }
        return filename.substring(0, lastDot);
    }

    // ===== 유틸리티 =====

    /**
     * 파일이 비어있는지 확인
     */
    public static boolean isEmpty(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            return files == null || files.length == 0;
        }
        return file.length() == 0;
    }

    /**
     * 파일이 비어있지 않은지 확인
     */
    public static boolean isNotEmpty(File file) {
        return !isEmpty(file);
    }
}
