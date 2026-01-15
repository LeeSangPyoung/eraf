package com.eraf.starter.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ERAF MyBatis Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(SqlSessionFactory.class)
@EnableConfigurationProperties(ErafMyBatisProperties.class)
public class ErafMyBatisAutoConfiguration {

}
