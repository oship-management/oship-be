package org.example.oshipserver.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisCacheConfig {

    // 캐시 TTL 설정
    private static final Duration DEFAULT_TTL = Duration.ofHours(6);
    private static final Duration CURRENT_MONTH_TTL = Duration.ofMinutes(5);
    private static final Duration PAST_MONTH_TTL = Duration.ofDays(7);

    // 캐시 이름
    public static final String CURRENT_MONTH_CACHE = "sellerStatsRedis:current";
    public static final String PAST_MONTH_CACHE = "sellerStatsRedis:past";

    /**
     * RedisCacheManager Bean 설정
     * - 캐시마다 TTL, 직렬화 전략 지정
     */
    @Primary
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Value 직렬화 방식: JSON 기반
        RedisSerializationContext.SerializationPair<Object> valueSerializer =
            RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer());

        // 기본 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(valueSerializer)
            .entryTtl(DEFAULT_TTL)
            .disableCachingNullValues();

        // 개별 캐시 설정 (TTL 포함)
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(
            CURRENT_MONTH_CACHE,
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(valueSerializer)
                .entryTtl(CURRENT_MONTH_TTL)
        );
        cacheConfigs.put(
            PAST_MONTH_CACHE,
            RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(valueSerializer)
                .entryTtl(PAST_MONTH_TTL)
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
