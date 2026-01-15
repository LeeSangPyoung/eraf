package com.eraf.core.file;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * ZIP 압축/해제 유틸리티
 */
public final class ZipUtils {

    private ZipUtils() {
    }

    /**
     * 파일들을 ZIP으로 압축
     */
    public static void compress(Path zipPath, List<Path> files) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);

            for (Path file : files) {
                if (file.toFile().isDirectory()) {
                    zipFile.addFolder(file.toFile(), parameters);
                } else {
                    zipFile.addFile(file.toFile(), parameters);
                }
            }
        }
    }

    /**
     * 디렉토리를 ZIP으로 압축
     */
    public static void compressDirectory(Path zipPath, Path directory) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            zipFile.addFolder(directory.toFile(), parameters);
        }
    }

    /**
     * 암호화된 ZIP 압축
     */
    public static void compressWithPassword(Path zipPath, List<Path> files, String password) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile(), password.toCharArray())) {
            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);

            for (Path file : files) {
                if (file.toFile().isDirectory()) {
                    zipFile.addFolder(file.toFile(), parameters);
                } else {
                    zipFile.addFile(file.toFile(), parameters);
                }
            }
        }
    }

    /**
     * ZIP 파일 압축 해제
     */
    public static void decompress(Path zipPath, Path destDirectory) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            zipFile.extractAll(destDirectory.toString());
        }
    }

    /**
     * 암호화된 ZIP 파일 압축 해제
     */
    public static void decompressWithPassword(Path zipPath, Path destDirectory, String password) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile(), password.toCharArray())) {
            zipFile.extractAll(destDirectory.toString());
        }
    }

    /**
     * ZIP 파일 내 특정 파일만 압축 해제
     */
    public static void extractFile(Path zipPath, String fileName, Path destDirectory) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            zipFile.extractFile(fileName, destDirectory.toString());
        }
    }

    /**
     * ZIP 파일 유효성 검사
     */
    public static boolean isValid(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            return zipFile.isValidZipFile();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ZIP 파일 암호화 여부 확인
     */
    public static boolean isEncrypted(Path zipPath) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            return zipFile.isEncrypted();
        }
    }
}
