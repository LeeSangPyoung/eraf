package com.eraf.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafStoragePropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafStorageProperties properties = new ErafStorageProperties();

        // Then
        assertNotNull(properties);
        assertEquals("local", properties.getType());
    }

    @Test
    @DisplayName("S3 설정")
    void testS3Settings() {
        // Given
        ErafStorageProperties properties = new ErafStorageProperties();
        ErafStorageProperties.S3 s3 = properties.getS3();

        // When
        properties.setType("s3");
        s3.setBucket("my-bucket");
        s3.setRegion("ap-northeast-2");

        // Then
        assertEquals("s3", properties.getType());
        assertEquals("my-bucket", s3.getBucket());
        assertEquals("ap-northeast-2", s3.getRegion());
    }

    @Test
    @DisplayName("로컬 저장소 설정")
    void testLocalSettings() {
        // Given
        ErafStorageProperties properties = new ErafStorageProperties();
        ErafStorageProperties.Local local = properties.getLocal();

        // When
        local.setBasePath("/data/uploads");

        // Then
        assertEquals("/data/uploads", local.getBasePath());
    }
}
