package com.eraf.starter.s3.local;

import com.eraf.starter.s3.ErafStorageProperties;
import com.eraf.starter.s3.StorageService;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 로컬 파일 시스템 저장소 구현
 */
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(ErafStorageProperties properties) {
        this.basePath = Paths.get(properties.getLocal().getBasePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory: " + basePath, e);
        }
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            Path targetPath = resolveAndCreateParent(path);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + path, e);
        }
    }

    @Override
    public String upload(String path, byte[] data, String contentType) {
        try {
            Path targetPath = resolveAndCreateParent(path);
            Files.write(targetPath, data);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + path, e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            Path filePath = resolve(path);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + path, e);
        }
    }

    @Override
    public byte[] downloadAsBytes(String path) {
        try {
            Path filePath = resolve(path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = resolve(path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = resolve(path);
        return Files.exists(filePath);
    }

    @Override
    public List<String> list(String prefix) {
        try {
            Path prefixPath = basePath.resolve(prefix).normalize();
            if (!Files.exists(prefixPath)) {
                return List.of();
            }

            try (Stream<Path> stream = Files.walk(prefixPath)) {
                return stream
                        .filter(Files::isRegularFile)
                        .map(p -> basePath.relativize(p).toString().replace("\\", "/"))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files: " + prefix, e);
        }
    }

    @Override
    public String getPresignedUrl(String path, int expirationMinutes) {
        // 로컬 저장소는 Presigned URL을 지원하지 않음
        throw new UnsupportedOperationException("Local storage does not support presigned URLs");
    }

    @Override
    public String getPresignedUploadUrl(String path, int expirationMinutes) {
        throw new UnsupportedOperationException("Local storage does not support presigned URLs");
    }

    @Override
    public void copy(String sourcePath, String targetPath) {
        try {
            Path source = resolve(sourcePath);
            Path target = resolveAndCreateParent(targetPath);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy file: " + sourcePath + " -> " + targetPath, e);
        }
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        try {
            Path source = resolve(sourcePath);
            Path target = resolveAndCreateParent(targetPath);
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file: " + sourcePath + " -> " + targetPath, e);
        }
    }

    private Path resolve(String path) {
        Path resolved = basePath.resolve(path).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new SecurityException("Path traversal attempt detected: " + path);
        }
        return resolved;
    }

    private Path resolveAndCreateParent(String path) throws IOException {
        Path resolved = resolve(path);
        Files.createDirectories(resolved.getParent());
        return resolved;
    }
}
