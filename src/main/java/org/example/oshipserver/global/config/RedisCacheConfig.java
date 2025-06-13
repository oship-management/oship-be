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

@Configuration
public class RedisCacheConfig {

    // 캐시 TTL 설정
    private static final Duration DEFAULT_TTL = Duration.ofHours(6);
    private static final Duration CURRENT_MONTH_TTL = Duration.ofMinutes(5);
    private static final Duration PAST_MONTH_TTL = Duration.ofDays(7);

    // 캐시 이름
    public static final String CURRENT_MONTH_CACHE = "sellerStatsRedis:current";
    public static final String PAST_MONTH_CACHE = "sellerStatsRedis:past";

    @Primary
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        // 기본 TTL 설정 (fallback)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(DEFAULT_TTL)
            .disableCachingNullValues();

        // 현재 월 캐시 TTL 설정
        RedisCacheConfiguration currentMonthConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(CURRENT_MONTH_TTL);

        // 과거 월 캐시 TTL 설정
        RedisCacheConfiguration pastMonthConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(PAST_MONTH_TTL);

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CURRENT_MONTH_CACHE, currentMonthConfig);
        cacheConfigs.put(PAST_MONTH_CACHE, pastMonthConfig);

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}
