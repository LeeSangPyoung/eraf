package com.eraf.ftp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafFtpPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // Then
        assertNotNull(properties);
        assertEquals(21, properties.getPort());
    }

    @Test
    @DisplayName("FTP 서버 설정")
    void testServerSettings() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // When
        properties.setHost("ftp.example.com");
        properties.setPort(2121);
        properties.setUsername("user");
        properties.setPassword("pass");

        // Then
        assertEquals("ftp.example.com", properties.getHost());
        assertEquals(2121, properties.getPort());
        assertEquals("user", properties.getUsername());
    }

    @Test
    @DisplayName("SFTP 모드 설정")
    void testSftpMode() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // Then - 기본값은 FTP
        assertEquals(ErafFtpProperties.FtpType.FTP, properties.getType());

        // When - SFTP로 변경
        properties.setType(ErafFtpProperties.FtpType.SFTP);
        properties.setPort(22);

        // Then
        assertEquals(ErafFtpProperties.FtpType.SFTP, properties.getType());
        assertEquals(22, properties.getPort());
    }

    @Test
    @DisplayName("타임아웃 및 모드 설정")
    void testTimeoutAndMode() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // Then - 기본값 확인
        assertEquals(10000, properties.getConnectionTimeout());
        assertEquals(30000, properties.getDataTimeout());
        assertTrue(properties.isPassiveMode());
        assertTrue(properties.isBinaryMode());

        // When
        properties.setConnectionTimeout(5000);
        properties.setDataTimeout(60000);
        properties.setPassiveMode(false);
        properties.setBinaryMode(false);

        // Then
        assertEquals(5000, properties.getConnectionTimeout());
        assertEquals(60000, properties.getDataTimeout());
        assertFalse(properties.isPassiveMode());
        assertFalse(properties.isBinaryMode());
    }
}
