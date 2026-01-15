package com.eraf.starter.tcp;

import com.eraf.starter.tcp.impl.NettyTcpClient;
import io.netty.channel.Channel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF TCP Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(Channel.class)
@EnableConfigurationProperties(ErafTcpProperties.class)
public class ErafTcpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TcpClient tcpClient(ErafTcpProperties properties) {
        return new NettyTcpClient(properties);
    }
}
