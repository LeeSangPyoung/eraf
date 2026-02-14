# ERAF Integration - S3

AWS S3 및 로컬 파일 저장소를 지원하는 통합 스토리지 모듈입니다.

## 기능

- **AWS S3**: Amazon S3 클라우드 스토리지
- **로컬 스토리지**: 로컬 파일 시스템 저장
- **통합 인터페이스**: S3/로컬 전환 시 코드 수정 불필요
- **Pre-signed URL**: 임시 접근 URL 생성
- **멀티파트 업로드**: 대용량 파일 분할 업로드

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-integration-s3</artifactId>
</dependency>
```

## 설정

### AWS S3 사용

```yaml
eraf:
  storage:
    type: s3
    s3:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
      region: ap-northeast-2
      bucket: my-bucket
      endpoint: https://s3.ap-northeast-2.amazonaws.com
```

### 로컬 스토리지 사용

```yaml
eraf:
  storage:
    type: local
    local:
      base-path: /var/storage
      create-directories: true
```

## 사용법

### 1. 기본 사용

```java
import com.eraf.s3.StorageService;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private final StorageService storageService;

    public FileService(StorageService storageService) {
        this.storageService = storageService;
    }

    public String uploadFile(String fileName, byte[] fileData) {
        String path = "uploads/" + fileName;
        return storageService.upload(path, fileData, "application/octet-stream");
    }

    public byte[] downloadFile(String path) {
        return storageService.downloadAsBytes(path);
    }

    public void deleteFile(String path) {
        storageService.delete(path);
    }
}
```

### 2. InputStream 사용

```java
import java.io.InputStream;

public String uploadFromStream(String fileName, InputStream inputStream, String contentType) {
    String path = "documents/" + fileName;
    return storageService.upload(path, inputStream, contentType);
}

public InputStream downloadAsStream(String path) {
    return storageService.download(path);
}
```

### 3. 파일 존재 확인

```java
public boolean fileExists(String path) {
    return storageService.exists(path);
}

public String uploadIfNotExists(String path, byte[] data) {
    if (!storageService.exists(path)) {
        return storageService.upload(path, data, "application/octet-stream");
    }
    return path;
}
```

### 4. 파일 목록 조회

```java
import java.util.List;

public List<String> listFiles(String prefix) {
    return storageService.list(prefix);
}

public List<String> listImagesInFolder() {
    return storageService.list("images/");
}
```

### 5. 파일 복사 및 이동

```java
public String copyFile(String sourcePath, String destinationPath) {
    return storageService.copy(sourcePath, destinationPath);
}

public String moveFile(String sourcePath, String destinationPath) {
    return storageService.move(sourcePath, destinationPath);
}
```

## 고급 기능

### 1. Pre-signed URL 생성

```java
import java.time.Duration;

public String generateDownloadUrl(String path) {
    // 1시간 동안 유효한 다운로드 URL
    return storageService.generatePresignedUrl(path, Duration.ofHours(1));
}

public String generateUploadUrl(String path) {
    // 30분 동안 유효한 업로드 URL (PUT)
    return storageService.generatePresignedUploadUrl(path, Duration.ofMinutes(30));
}
```

### 2. 멀티파트 업로드

```java
import com.amazonaws.services.s3.model.PartETag;
import java.util.List;

public String uploadLargeFile(String path, InputStream largeFile, long fileSize) {
    // 5MB 이상 파일은 자동으로 멀티파트 업로드
    return storageService.uploadLarge(path, largeFile, fileSize, "application/octet-stream");
}
```

### 3. 메타데이터 설정

```java
import java.util.Map;

public String uploadWithMetadata(String path, byte[] data) {
    Map<String, String> metadata = Map.of(
        "uploader", "user123",
        "upload-date", LocalDateTime.now().toString(),
        "file-type", "invoice"
    );
    return storageService.uploadWithMetadata(path, data, "application/pdf", metadata);
}

public Map<String, String> getMetadata(String path) {
    return storageService.getMetadata(path);
}
```

### 4. Public Read 설정

```yaml
eraf:
  storage:
    s3:
      default-acl: public-read
```

```java
// Public으로 업로드
public String uploadPublicFile(String path, byte[] data) {
    return storageService.uploadPublic(path, data, "image/jpeg");
}

// Public URL 생성
public String getPublicUrl(String path) {
    return storageService.getPublicUrl(path);
}
```

## 환경별 전환

### 개발 환경 (로컬 스토리지)

```yaml
# application-dev.yml
eraf:
  storage:
    type: local
    local:
      base-path: /tmp/dev-storage
```

### 운영 환경 (S3)

```yaml
# application-prod.yml
eraf:
  storage:
    type: s3
    s3:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
      region: ap-northeast-2
      bucket: prod-bucket
```

## S3 버킷 정책

### CORS 설정

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["https://example.com"],
    "ExposeHeaders": ["ETag"]
  }
]
```

### 버킷 정책

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::my-bucket/public/*"
    }
  ]
}
```

## 에러 처리

```java
import com.amazonaws.AmazonServiceException;

public String uploadWithErrorHandling(String path, byte[] data) {
    try {
        return storageService.upload(path, data, "application/octet-stream");
    } catch (AmazonServiceException e) {
        if (e.getStatusCode() == 403) {
            throw new BusinessException("S3 access denied");
        } else if (e.getStatusCode() == 404) {
            throw new BusinessException("Bucket not found");
        }
        throw new BusinessException("S3 upload failed: " + e.getMessage());
    }
}
```

## 성능 최적화

### 1. 병렬 업로드

```java
import java.util.concurrent.CompletableFuture;
import java.util.List;

public List<String> uploadMultipleFiles(List<FileData> files) {
    List<CompletableFuture<String>> futures = files.stream()
        .map(file -> CompletableFuture.supplyAsync(() ->
            storageService.upload(file.getPath(), file.getData(), file.getContentType())
        ))
        .toList();

    return futures.stream()
        .map(CompletableFuture::join)
        .toList();
}
```

### 2. Transfer Acceleration

```yaml
eraf:
  storage:
    s3:
      use-transfer-acceleration: true
```

### 3. CloudFront CDN

```yaml
eraf:
  storage:
    s3:
      cloudfront-domain: d123456abcdef8.cloudfront.net
```

```java
public String getCdnUrl(String path) {
    return storageService.getCdnUrl(path);
}
```

## 보안

### 1. 암호화 (SSE)

```yaml
eraf:
  storage:
    s3:
      server-side-encryption: AES256  # 또는 aws:kms
```

### 2. IAM 역할 사용

```yaml
eraf:
  storage:
    s3:
      use-iam-role: true  # EC2 IAM Role 사용
```

### 3. 버킷 버전 관리

```yaml
eraf:
  storage:
    s3:
      versioning-enabled: true
```

## 모범 사례

1. **환경별 분리**: 개발/스테이징/운영 버킷 분리
2. **경로 규칙**: 일관된 경로 네이밍 규칙 (예: `/{type}/{year}/{month}/{filename}`)
3. **Content-Type**: 올바른 MIME 타입 설정
4. **Lifecycle 정책**: 오래된 파일 자동 삭제/아카이빙
5. **암호화**: 민감한 파일은 SSE 활성화
6. **Pre-signed URL**: 직접 노출보다 임시 URL 사용
7. **IAM Role**: Access Key보다 IAM Role 사용 권장

## 참고

- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)
- [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/)
- [S3 Best Practices](https://docs.aws.amazon.com/AmazonS3/latest/userguide/best-practices.html)
