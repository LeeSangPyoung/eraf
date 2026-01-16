package com.eraf.core.file;

import java.time.LocalDateTime;

/**
 * 저장된 파일 정보
 */
public class StoredFile {

    /**
     * 원본 파일명
     */
    private String originalFilename;

    /**
     * 저장된 파일명
     */
    private String storedFilename;

    /**
     * 저장 경로 (디렉토리)
     */
    private String directory;

    /**
     * 전체 파일 경로
     */
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    private long size;

    /**
     * 컨텐츠 타입 (MIME)
     */
    private String contentType;

    /**
     * 파일 확장자
     */
    private String extension;

    /**
     * 저장 시간
     */
    private LocalDateTime storedAt;

    /**
     * 파일 접근 URL
     */
    private String url;

    /**
     * 체크섬 (MD5)
     */
    private String checksum;

    public StoredFile() {
        this.storedAt = LocalDateTime.now();
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StoredFile storedFile = new StoredFile();

        public Builder originalFilename(String originalFilename) {
            storedFile.originalFilename = originalFilename;
            return this;
        }

        public Builder storedFilename(String storedFilename) {
            storedFile.storedFilename = storedFilename;
            return this;
        }

        public Builder directory(String directory) {
            storedFile.directory = directory;
            return this;
        }

        public Builder filePath(String filePath) {
            storedFile.filePath = filePath;
            return this;
        }

        public Builder size(long size) {
            storedFile.size = size;
            return this;
        }

        public Builder contentType(String contentType) {
            storedFile.contentType = contentType;
            return this;
        }

        public Builder extension(String extension) {
            storedFile.extension = extension;
            return this;
        }

        public Builder storedAt(LocalDateTime storedAt) {
            storedFile.storedAt = storedAt;
            return this;
        }

        public Builder url(String url) {
            storedFile.url = url;
            return this;
        }

        public Builder checksum(String checksum) {
            storedFile.checksum = checksum;
            return this;
        }

        public StoredFile build() {
            return storedFile;
        }
    }

    // Getters and Setters
    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public LocalDateTime getStoredAt() {
        return storedAt;
    }

    public void setStoredAt(LocalDateTime storedAt) {
        this.storedAt = storedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * 사람이 읽기 쉬운 파일 크기 반환
     */
    public String getHumanReadableSize() {
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

    @Override
    public String toString() {
        return "StoredFile{" +
                "originalFilename='" + originalFilename + '\'' +
                ", storedFilename='" + storedFilename + '\'' +
                ", filePath='" + filePath + '\'' +
                ", size=" + getHumanReadableSize() +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
