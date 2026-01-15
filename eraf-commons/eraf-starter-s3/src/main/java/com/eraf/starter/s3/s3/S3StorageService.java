package com.eraf.starter.s3.s3;

import com.eraf.starter.s3.ErafStorageProperties;
import com.eraf.starter.s3.StorageService;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AWS S3/MinIO 저장소 구현
 */
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, ErafStorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = properties.getS3().getBucket();
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            byte[] data = inputStream.readAllBytes();
            return upload(path, data, contentType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + path, e);
        }
    }

    @Override
    public String upload(String path, byte[] data, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return path;
    }

    @Override
    public InputStream download(String path) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        return s3Client.getObject(request);
    }

    @Override
    public byte[] downloadAsBytes(String path) {
        try (InputStream inputStream = download(path)) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        s3Client.deleteObject(request);
    }

    @Override
    public boolean exists(String path) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public List<String> list(String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    @Override
    public String getPresignedUrl(String path, int expirationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public String getPresignedUploadUrl(String path, int expirationMinutes) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    @Override
    public void copy(String sourcePath, String targetPath) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(sourcePath)
                .destinationBucket(bucket)
                .destinationKey(targetPath)
                .build();

        s3Client.copyObject(request);
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        copy(sourcePath, targetPath);
        delete(sourcePath);
    }
}
