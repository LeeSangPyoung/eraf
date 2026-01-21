package com.eraf.starter.s3;

import com.eraf.starter.s3.local.LocalStorageService;
import com.eraf.starter.s3.s3.S3StorageService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * ERAF 파일 저장소 Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafStorageProperties.class)
public class ErafStorageAutoConfiguration {

    /**
     * 로컬 저장소 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.storage.type", havingValue = "local", matchIfMissing = true)
    public static class LocalStorageConfiguration {

        @Bean
        @ConditionalOnMissingBean(StorageService.class)
        public StorageService localStorageService(ErafStorageProperties properties) {
            return new LocalStorageService(properties);
        }
    }

    /**
     * S3 저장소 설정
     */
    @Configuration
    @ConditionalOnClass(S3Client.class)
    @ConditionalOnProperty(name = "eraf.storage.type", havingValue = "s3")
    public static class S3StorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public S3Client s3Client(ErafStorageProperties properties) {
            ErafStorageProperties.S3 s3Props = properties.getS3();

            S3ClientBuilder builder = S3Client.builder()
                    .region(Region.of(s3Props.getRegion()));

            if (s3Props.getAccessKey() != null && s3Props.getSecretKey() != null) {
                builder.credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                        )
                );
            }

            return builder.build();
        }

        @Bean
        @ConditionalOnMissingBean
        public S3Presigner s3Presigner(ErafStorageProperties properties) {
            ErafStorageProperties.S3 s3Props = properties.getS3();

            S3Presigner.Builder builder = S3Presigner.builder()
                    .region(Region.of(s3Props.getRegion()));

            if (s3Props.getAccessKey() != null && s3Props.getSecretKey() != null) {
                builder.credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                        )
                );
            }

            return builder.build();
        }

        @Bean
        @ConditionalOnMissingBean(StorageService.class)
        public StorageService s3StorageService(S3Client s3Client, S3Presigner s3Presigner,
                                                ErafStorageProperties properties) {
            return new S3StorageService(s3Client, s3Presigner, properties);
        }
    }

    /**
     * MinIO 저장소 설정
     */
    @Configuration
    @ConditionalOnClass(S3Client.class)
    @ConditionalOnProperty(name = "eraf.storage.type", havingValue = "minio")
    public static class MinioStorageConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public S3Client minioClient(ErafStorageProperties properties) {
            ErafStorageProperties.S3 s3Props = properties.getS3();

            return S3Client.builder()
                    .endpointOverride(URI.create(s3Props.getEndpoint()))
                    .region(Region.of(s3Props.getRegion()))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                            )
                    )
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    )
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean
        public S3Presigner minioPresigner(ErafStorageProperties properties) {
            ErafStorageProperties.S3 s3Props = properties.getS3();

            return S3Presigner.builder()
                    .endpointOverride(URI.create(s3Props.getEndpoint()))
                    .region(Region.of(s3Props.getRegion()))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(s3Props.getAccessKey(), s3Props.getSecretKey())
                            )
                    )
                    .serviceConfiguration(
                            S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build()
                    )
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean(StorageService.class)
        public StorageService minioStorageService(S3Client minioClient, S3Presigner minioPresigner,
                                                   ErafStorageProperties properties) {
            return new S3StorageService(minioClient, minioPresigner, properties);
        }
    }
}
