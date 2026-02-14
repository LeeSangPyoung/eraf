# ERAF Integration - FTP

FTP/SFTP 기반 파일 전송을 지원하는 모듈입니다.

## 기능

- **FTP 클라이언트**: 표준 FTP 프로토콜 지원
- **SFTP 클라이언트**: SSH 기반 보안 파일 전송
- **파일 업로드/다운로드**: 스트림 및 바이트 배열 지원
- **디렉토리 관리**: 생성, 삭제, 목록 조회
- **자동 재연결**: 연결 실패 시 자동 재시도

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-integration-ftp</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
eraf:
  ftp:
    host: ftp.example.com
    port: 21
    username: user
    password: password
    protocol: ftp  # ftp 또는 sftp
    passive-mode: true
    connection-timeout: 30000
    data-timeout: 60000
    control-encoding: UTF-8
```

## 사용법

### 1. FTP 클라이언트 사용

```java
import com.eraf.ftp.FtpClient;
import com.eraf.ftp.impl.FtpClientImpl;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private final FtpClient ftpClient;

    public FileService(ErafFtpProperties properties) {
        this.ftpClient = new FtpClientImpl(properties);
    }

    public void uploadFile(String remotePath, byte[] fileData) {
        ftpClient.connect();
        try {
            boolean success = ftpClient.upload(remotePath, fileData);
            if (!success) {
                throw new RuntimeException("Upload failed");
            }
        } finally {
            ftpClient.disconnect();
        }
    }

    public byte[] downloadFile(String remotePath) {
        ftpClient.connect();
        try {
            return ftpClient.downloadAsBytes(remotePath);
        } finally {
            ftpClient.disconnect();
        }
    }
}
```

### 2. SFTP 클라이언트 사용

```yaml
eraf:
  ftp:
    host: sftp.example.com
    port: 22
    username: user
    password: password
    protocol: sftp
```

```java
import com.eraf.ftp.impl.SftpClientImpl;

@Service
public class SecureFileService {

    private final FtpClient sftpClient;

    public SecureFileService(ErafFtpProperties properties) {
        this.sftpClient = new SftpClientImpl(properties);
    }

    public void uploadSecure(String remotePath, InputStream inputStream) {
        sftpClient.connect();
        try {
            sftpClient.upload(remotePath, inputStream);
        } finally {
            sftpClient.disconnect();
        }
    }
}
```

### 3. 디렉토리 관리

```java
@Service
public class DirectoryService {

    private final FtpClient ftpClient;

    public void manageDirectories() {
        ftpClient.connect();
        try {
            // 디렉토리 생성
            ftpClient.makeDirectory("/upload/2024");

            // 파일 목록 조회
            List<String> files = ftpClient.listFiles("/upload");
            files.forEach(System.out::println);

            // 파일 존재 여부 확인
            boolean exists = ftpClient.exists("/upload/file.txt");

            // 디렉토리 변경
            ftpClient.changeDirectory("/upload");

            // 작업 디렉토리 조회
            String pwd = ftpClient.getWorkingDirectory();
            System.out.println("Current directory: " + pwd);

        } finally {
            ftpClient.disconnect();
        }
    }
}
```

### 4. 파일 이름 변경 및 이동

```java
public void renameFile(String oldPath, String newPath) {
    ftpClient.connect();
    try {
        boolean success = ftpClient.rename(oldPath, newPath);
        if (!success) {
            throw new RuntimeException("Rename failed");
        }
    } finally {
        ftpClient.disconnect();
    }
}
```

## 고급 기능

### 1. Passive/Active 모드

```yaml
eraf:
  ftp:
    passive-mode: true  # Passive 모드 (방화벽 친화적)
```

### 2. Binary/ASCII 전송 모드

```java
@Configuration
public class FtpConfig {

    @Bean
    public FtpClient ftpClient(ErafFtpProperties properties) {
        FtpClientImpl client = new FtpClientImpl(properties);
        client.setFileType(FTP.BINARY_FILE_TYPE);  // Binary 모드
        return client;
    }
}
```

### 3. Keep-Alive

```yaml
eraf:
  ftp:
    keep-alive-timeout: 300  # 300초
```

### 4. 연결 풀링

```java
import org.apache.commons.pool2.impl.GenericObjectPool;

@Configuration
public class FtpPoolConfig {

    @Bean
    public GenericObjectPool<FtpClient> ftpClientPool(ErafFtpProperties properties) {
        FtpClientFactory factory = new FtpClientFactory(properties);
        GenericObjectPoolConfig<FtpClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(2);
        return new GenericObjectPool<>(factory, poolConfig);
    }
}

@Service
public class PooledFileService {

    private final GenericObjectPool<FtpClient> clientPool;

    public void uploadWithPool(String remotePath, byte[] data) {
        FtpClient client = null;
        try {
            client = clientPool.borrowObject();
            client.upload(remotePath, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                clientPool.returnObject(client);
            }
        }
    }
}
```

## 에러 처리

```java
import java.io.IOException;

@Service
public class SafeFileService {

    private final FtpClient ftpClient;

    public void uploadWithRetry(String remotePath, byte[] data, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                ftpClient.connect();
                boolean success = ftpClient.upload(remotePath, data);
                if (success) {
                    return;
                }
                attempts++;
            } catch (Exception e) {
                attempts++;
                if (attempts >= maxRetries) {
                    throw new RuntimeException("Upload failed after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(1000 * attempts);  // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                ftpClient.disconnect();
            }
        }
    }
}
```

## SSH Key 인증 (SFTP)

```yaml
eraf:
  ftp:
    protocol: sftp
    host: sftp.example.com
    port: 22
    username: user
    private-key-path: /path/to/private_key
    passphrase: optional_passphrase
```

## 보안

### 1. 암호화된 비밀번호 사용

```yaml
eraf:
  ftp:
    password: '{cipher}AQA...'  # Spring Cloud Config 암호화
```

### 2. SSL/TLS (FTPS)

```yaml
eraf:
  ftp:
    protocol: ftps
    implicit: false  # Explicit FTPS
    trust-all-certificates: false
```

## 모범 사례

1. **연결 관리**: try-finally로 반드시 disconnect 호출
2. **Passive 모드**: 방화벽 환경에서는 passive-mode: true 사용
3. **Binary 모드**: 이진 파일 전송 시 BINARY_FILE_TYPE 설정
4. **연결 풀링**: 빈번한 FTP 작업 시 연결 풀 사용
5. **재시도 로직**: 네트워크 불안정 시 재시도 메커니즘 구현
6. **SSH Key**: SFTP는 비밀번호보다 SSH Key 인증 권장

## 참고

- [Apache Commons Net](https://commons.apache.org/proper/commons-net/)
- [JSch (SFTP)](http://www.jcraft.com/jsch/)
- [FTP RFC 959](https://www.ietf.org/rfc/rfc959.txt)
