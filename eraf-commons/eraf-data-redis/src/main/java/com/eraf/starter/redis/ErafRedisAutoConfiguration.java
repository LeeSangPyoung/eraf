package com.eraf.starter.redis;

import com.eraf.core.idempotent.IdempotencyStore;
import com.eraf.core.idempotent.IdempotentAspect;
import com.eraf.core.lock.DistributedLockAspect;
import com.eraf.core.lock.LockProvider;
import com.eraf.core.lock.OptimisticRetryAspect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * ERAF Redis Auto Configuration
 */
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@EnableConfigurationProperties(ErafRedisProperties.class)
public class ErafRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value serializer with type info
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Redis 기반 분산 락 제공자
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
    public LockProvider redisLockProvider(StringRedisTemplate stringRedisTemplate) {
        return new RedisLockProvider(stringRedisTemplate);
    }

    /**
     * 분산 락 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
    public DistributedLockAspect distributedLockAspect(LockProvider lockProvider) {
        return new DistributedLockAspect(lockProvider);
    }

    /**
     * Redis 기반 시퀀스 생성기
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.sequence.enabled", havingValue = "true", matchIfMissing = true)
    public RedisSequenceGenerator redisSequenceGenerator(StringRedisTemplate stringRedisTemplate) {
        return new RedisSequenceGenerator(stringRedisTemplate);
    }

    /**
     * Redis 기반 멱등성 저장소
     */
    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    @ConditionalOnProperty(name = "eraf.redis.idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotencyStore redisIdempotencyStore(
            RedisTemplate<String, Object> redisTemplate,
            ErafRedisProperties properties) {
        Duration ttl = properties.getIdempotent().getTtl();
        return new RedisIdempotencyStore(redisTemplate, ttl);
    }

    /**
     * 멱등성 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotentAspect idempotentAspect(IdempotencyStore idempotencyStore) {
        return new IdempotentAspect(idempotencyStore);
    }

    /**
     * 낙관적 락 재시도 AOP Aspect
     */
    @Bean
    @ConditionalOnMissingBean
    public OptimisticRetryAspect optimisticRetryAspect() {
        return new OptimisticRetryAspect();
    }
}
