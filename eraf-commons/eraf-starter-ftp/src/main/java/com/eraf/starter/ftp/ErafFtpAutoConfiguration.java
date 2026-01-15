package com.eraf.starter.ftp;

import com.eraf.starter.ftp.impl.FtpClientImpl;
import com.eraf.starter.ftp.impl.SftpClientImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF FTP/SFTP Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafFtpProperties.class)
public class ErafFtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.ftp.type", havingValue = "ftp", matchIfMissing = true)
    public FtpClient ftpClient(ErafFtpProperties properties) {
        return new FtpClientImpl(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.ftp.type", havingValue = "sftp")
    public FtpClient sftpClient(ErafFtpProperties properties) {
        return new SftpClientImpl(properties);
    }
}
