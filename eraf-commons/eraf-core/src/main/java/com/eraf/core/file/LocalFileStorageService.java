package com.eraf.core.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 로컬 파일 시스템 저장소 서비스 구현체
 */
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path rootLocation;
    private final String baseUrl;
    private final boolean createDateDirectory;

    public LocalFileStorageService(String uploadPath) {
        this(uploadPath, "/files", true);
    }

    public LocalFileStorageService(String uploadPath, String baseUrl, boolean createDateDirectory) {
        this.rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        this.createDateDirectory = createDateDirectory;
        init();
    }

    /**
     * 저장소 초기화
     */
    private void init() {
        try {
            Files.createDirectories(rootLocation);
            log.info("File storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize file storage", e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file) {
        return store(file, getDateDirectory());
    }

    @Override
    public StoredFile store(MultipartFile file, String directory) {
        String storedFilename = generateFilename(file.getOriginalFilename());
        return store(file, directory, storedFilename);
    }

    @Override
    public StoredFile store(MultipartFile file, String directory, String filename) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Cannot store empty file");
            }

            validateFilename(filename);

            Path targetDirectory = rootLocation.resolve(directory).normalize();
            Files.createDirectories(targetDirectory);

            Path targetPath = targetDirectory.resolve(filename).normalize();

            // Path traversal 공격 방지
            if (!targetPath.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot store file outside upload directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String filePath = directory + "/" + filename;
            String checksum = calculateChecksum(targetPath);

            return StoredFile.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(filename)
                    .directory(directory)
                    .filePath(filePath)
                    .size(file.getSize())
                    .contentType(file.getContentType())
                    .extension(getExtension(file.getOriginalFilename()))
                    .url(getUrl(filePath))
                    .checksum(checksum)
                    .build();

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public StoredFile store(InputStream inputStream, String directory, String filename, String contentType) {
        try {
            validateFilename(filename);

            Path targetDirectory = rootLocation.resolve(directory).normalize();
            Files.createDirectories(targetDirectory);

            Path targetPath = targetDirectory.resolve(filename).normalize();

            if (!targetPath.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot store file outside upload directory");
            }

            long size = Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            String filePath = directory + "/" + filename;
            String checksum = calculateChecksum(targetPath);

            return StoredFile.builder()
                    .originalFilename(filename)
                    .storedFilename(filename)
                    .directory(directory)
                    .filePath(filePath)
                    .size(size)
                    .contentType(contentType)
                    .extension(getExtension(filename))
                    .url(getUrl(filePath))
                    .checksum(checksum)
                    .build();

        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + filename, e);
        }
    }

    @Override
    public List<StoredFile> storeAll(List<MultipartFile> files) {
        return storeAll(files, getDateDirectory());
    }

    @Override
    public List<StoredFile> storeAll(List<MultipartFile> files, String directory) {
        List<StoredFile> storedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                storedFiles.add(store(file, directory));
            }
        }
        return storedFiles;
    }

    @Override
    public Resource load(String filePath) {
        try {
            Path file = getPath(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found: " + filePath, e);
        }
    }

    @Override
    public Path getPath(String filePath) {
        return rootLocation.resolve(filePath).normalize();
    }

    @Override
    public boolean exists(String filePath) {
        Path path = getPath(filePath);
        return Files.exists(path) && path.startsWith(rootLocation);
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path path = getPath(filePath);
            if (!path.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot delete file outside upload directory");
            }
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    @Override
    public int deleteAll(List<String> filePaths) {
        int deletedCount = 0;
        for (String filePath : filePaths) {
            if (delete(filePath)) {
                deletedCount++;
            }
        }
        return deletedCount;
    }

    @Override
    public int deleteDirectory(String directory) {
        try {
            Path dirPath = rootLocation.resolve(directory).normalize();
            if (!dirPath.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot delete directory outside upload directory");
            }

            if (!Files.exists(dirPath)) {
                return 0;
            }

            int[] count = {0};
            try (Stream<Path> paths = Files.walk(dirPath)) {
                paths.sorted((p1, p2) -> -p1.compareTo(p2))
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                                if (!Files.isDirectory(path)) {
                                    count[0]++;
                                }
                            } catch (IOException e) {
                                log.error("Failed to delete: {}", path, e);
                            }
                        });
            }
            return count[0];
        } catch (IOException e) {
            log.error("Failed to delete directory: {}", directory, e);
            return 0;
        }
    }

    @Override
    public boolean copy(String sourcePath, String destinationPath) {
        try {
            Path source = getPath(sourcePath);
            Path destination = getPath(destinationPath);

            if (!source.startsWith(rootLocation) || !destination.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot copy file outside upload directory");
            }

            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Failed to copy file from {} to {}", sourcePath, destinationPath, e);
            return false;
        }
    }

    @Override
    public boolean move(String sourcePath, String destinationPath) {
        try {
            Path source = getPath(sourcePath);
            Path destination = getPath(destinationPath);

            if (!source.startsWith(rootLocation) || !destination.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot move file outside upload directory");
            }

            Files.createDirectories(destination.getParent());
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            log.error("Failed to move file from {} to {}", sourcePath, destinationPath, e);
            return false;
        }
    }

    @Override
    public String getUrl(String filePath) {
        return baseUrl + "/" + filePath;
    }

    /**
     * 날짜 기반 디렉토리 경로 생성
     */
    private String getDateDirectory() {
        if (createDateDirectory) {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }
        return "";
    }

    /**
     * UUID 기반 파일명 생성
     */
    private String generateFilename(String originalFilename) {
        String extension = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return extension.isEmpty() ? uuid : uuid + "." + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }

    /**
     * 파일명 유효성 검사
     */
    private void validateFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new FileStorageException("Filename cannot be empty");
        }
        if (filename.contains("..")) {
            throw new FileStorageException("Invalid filename: " + filename);
        }
    }

    /**
     * 파일 체크섬 계산 (MD5)
     */
    private String calculateChecksum(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] digest = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            log.warn("Failed to calculate checksum for: {}", filePath, e);
            return null;
        }
    }
}
