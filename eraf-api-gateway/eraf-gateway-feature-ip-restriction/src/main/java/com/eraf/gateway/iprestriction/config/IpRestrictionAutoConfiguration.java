package com.eraf.gateway.iprestriction.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.iprestriction.filter.IpRestrictionFilter;
import com.eraf.gateway.iprestriction.repository.IpRestrictionRepository;
import com.eraf.gateway.iprestriction.repository.InMemoryIpRestrictionRepository;
import com.eraf.gateway.iprestriction.service.IpRestrictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IP Restriction 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(IpRestrictionFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.ip-restriction", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IpRestrictionProperties.class)
public class IpRestrictionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IpRestrictionRepository ipRestrictionRepository() {
        log.info("Initializing InMemoryIpRestrictionRepository");
        return new InMemoryIpRestrictionRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public IpRestrictionService ipRestrictionService(IpRestrictionRepository ipRestrictionRepository) {
        log.info("Initializing IpRestrictionService");
        return new IpRestrictionService(ipRestrictionRepository);
    }

    @Bean
    public FilterRegistrationBean<IpRestrictionFilter> ipRestrictionFilterRegistration(
            IpRestrictionService ipRestrictionService,
            IpRestrictionProperties properties) {
        log.info("Registering IpRestrictionFilter with order: {}", FilterOrder.IP_RESTRICTION);

        IpRestrictionFilter filter = new IpRestrictionFilter(
                ipRestrictionService,
                properties.getExcludePatterns()
        );

        FilterRegistrationBean<IpRestrictionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.IP_RESTRICTION);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
