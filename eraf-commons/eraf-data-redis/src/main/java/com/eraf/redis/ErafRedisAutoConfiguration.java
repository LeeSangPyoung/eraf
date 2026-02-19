package com.eraf.redis;

import com.eraf.idempotent.IdempotencyStore;
import com.eraf.lock.LockProvider;
import com.eraf.redis.invalidation.RedisCacheInvalidator;
import com.eraf.redis.lock.LockStatistics;
import com.eraf.redis.lock.RedisReadWriteLock;
import com.eraf.redis.pubsub.RedisMessagePublisher;
import com.eraf.redis.pubsub.RedisMessageSubscriber;
import com.eraf.redis.statistics.RedisCacheStatistics;
import com.eraf.redis.stream.RedisStreamConsumer;
import com.eraf.redis.stream.RedisStreamProducer;
import com.eraf.redis.warming.CacheWarmer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
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
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                        ErafRedisProperties properties) {
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

        // Apply transaction support from properties
        if (properties.isEnableTransactions()) {
            template.setEnableTransactionSupport(true);
        }

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * 분산 락 통계
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
    public LockStatistics lockStatistics() {
        return new LockStatistics();
    }

    /**
     * Redis 기반 분산 락 제공자
     */
    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    @ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
    public RedisLockProvider redisLockProvider(
            StringRedisTemplate stringRedisTemplate,
            LockStatistics lockStatistics,
            ErafRedisProperties properties) {
        RedisLockProvider provider = new RedisLockProvider(stringRedisTemplate);
        provider.enableStatistics(lockStatistics);
        if (properties.getLock().isWatchdogEnabled()) {
            provider.enableWatchdog();
        }
        return provider;
    }

    /**
     * Redis ReadWrite Lock
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.lock.enabled", havingValue = "true", matchIfMissing = true)
    public RedisReadWriteLock redisReadWriteLock(StringRedisTemplate stringRedisTemplate) {
        return new RedisReadWriteLock(stringRedisTemplate);
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
     * Redis 메시지 리스너 컨테이너
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.invalidation.enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis 캐시 통계
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.statistics.enabled", havingValue = "true", matchIfMissing = true)
    public RedisCacheStatistics redisCacheStatistics(StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheStatistics(stringRedisTemplate);
    }

    /**
     * 캐시 워머
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.warming.enabled", havingValue = "true", matchIfMissing = true)
    public CacheWarmer cacheWarmer(RedisTemplate<String, Object> redisTemplate,
                                    ErafRedisProperties properties) {
        return new CacheWarmer(redisTemplate, properties.getWarming().getParallelThreads());
    }

    /**
     * Redis Pub/Sub 캐시 무효화
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.invalidation.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(CacheManager.class)
    public RedisCacheInvalidator redisCacheInvalidator(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            CacheManager cacheManager) {
        return new RedisCacheInvalidator(redisTemplate, listenerContainer, cacheManager);
    }

    /**
     * Redis Pub/Sub 메시지 발행자
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.pubsub.enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessagePublisher redisMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        return new RedisMessagePublisher(redisTemplate);
    }

    /**
     * Redis Pub/Sub 메시지 구독자
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.pubsub.enabled", havingValue = "true", matchIfMissing = true)
    public RedisMessageSubscriber redisMessageSubscriber(RedisMessageListenerContainer listenerContainer) {
        return new RedisMessageSubscriber(listenerContainer);
    }

    /**
     * Redis Streams 메시지 생산자
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.stream.enabled", havingValue = "true", matchIfMissing = true)
    public RedisStreamProducer redisStreamProducer(StringRedisTemplate stringRedisTemplate) {
        return new RedisStreamProducer(stringRedisTemplate);
    }

    /**
     * Redis Streams 메시지 소비자
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.redis.stream.enabled", havingValue = "true", matchIfMissing = true)
    public RedisStreamConsumer redisStreamConsumer(StringRedisTemplate stringRedisTemplate) {
        return new RedisStreamConsumer(stringRedisTemplate);
    }
}
