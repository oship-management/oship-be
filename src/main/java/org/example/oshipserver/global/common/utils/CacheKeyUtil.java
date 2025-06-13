package org.example.oshipserver.global.common.utils;

import java.time.YearMonth;

public class CacheKeyUtil {

    private static final String LOCAL_STATS_PREFIX = "seller:dashboard:"; // 로컬 캐시용
    private static final String REDIS_STATS_PREFIX = "cache:seller:monthly-stats:"; // Redis 캐시

    /**
     * 로컬 캐시용 키 생성
     */
    public static String getLocalMonthlyStatsKey(Long sellerId, String monthStr) {
        return LOCAL_STATS_PREFIX + sellerId + ":" + monthStr.replace("-", "");
    }

    /**
     * Redis 현재 월 캐시 키 생성
     */
    public static String getRedisCurrentMonthStatsKey(Long sellerId) {
        String nowMonth = YearMonth.now().toString().replace("-", "");
        return REDIS_STATS_PREFIX + "current:" + sellerId + ":" + nowMonth;
    }

    /**
     * Redis 과거 월 캐시 키 생성
     */
    public static String getRedisPastMonthStatsKey(Long sellerId, String monthStr) {
        return REDIS_STATS_PREFIX + "past:" + sellerId + ":" + monthStr.replace("-", "");
    }
}
