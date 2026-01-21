package com.eraf.starter.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ERAF 파일 저장소 설정
 */
@ConfigurationProperties(prefix = "eraf.storage")
public class ErafStorageProperties {

    /**
     * 저장소 타입 (local, s3, minio)
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * 로컬 저장소 설정
     */
    private Local local = new Local();

    /**
     * S3/MinIO 설정
     */
    private S3 s3 = new S3();

    public enum StorageType {
        LOCAL, S3, MINIO
    }

    public static class Local {
        /**
         * 로컬 저장 경로
         */
        private String basePath = "./storage";

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class S3 {
        /**
         * 버킷 이름
         */
        private String bucket;

        /**
         * 리전
         */
        private String region = "ap-northeast-2";

        /**
         * 엔드포인트 (MinIO용)
         */
        private String endpoint;

        /**
         * Access Key
         */
        private String accessKey;

        /**
         * Secret Key
         */
        private String secretKey;

        /**
         * Path Style Access (MinIO용)
         */
        private boolean pathStyleAccessEnabled = false;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyleAccessEnabled() {
            return pathStyleAccessEnabled;
        }

        public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
            this.pathStyleAccessEnabled = pathStyleAccessEnabled;
        }
    }

    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }
}
