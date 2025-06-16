package org.example.oshipserver.domain.order.service;

import static org.example.oshipserver.global.config.RedisCacheConfig.CURRENT_MONTH_CACHE;
import static org.example.oshipserver.global.config.RedisCacheConfig.PAST_MONTH_CACHE;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.global.common.utils.CacheKeyUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatsCacheService {

    private final OrderRepository orderRepository;
    private final OrderStatsCalculator calculator;


    public OrderStatsResponse getFreshStats(Long sellerId, String monthStr) {
        log.info("[NO CACHE] 현재 월 → 실시간 계산");
        return calculateStats(sellerId, monthStr);
    }

    public OrderStatsResponse getCachedMonthlyStatsFromLocal(Long sellerId, String monthStr) {
        return getCachedMonthlyStats(sellerId, monthStr);
    }


    /**
     * [v2] 캐시 적용된 월별 통계 API
     * - 현재 월: 캐시 사용하지 않고 항상 fresh하게 조회
     * - 과거 월: 로컬 캐시 적용 (예: Caffeine)
     */
    public OrderStatsResponse getMonthlyStatsV2(Long sellerId, String monthStr) {
        if (isCurrentMonth(monthStr)) {
            log.info("[NO CACHE] CURRENT MONTH → 실시간 조회 sellerId={}, monthStr={}", sellerId, monthStr);
            return calculateStats(sellerId, monthStr);
        }
        return getCachedMonthlyStats(sellerId, monthStr);
    }

    /**
     * [v3] Redis 캐시 기반 월별 통계 API
     * - 현재 월: Redis 캐시 사용 (TTL: 5분)
     * - 과거 월: Redis 캐시 사용 (TTL: 7일)
     */
    public OrderStatsResponse getMonthlyStatsV3(Long sellerId, String monthStr) {
        if (isCurrentMonth(monthStr)) {
            return getCachedCurrentMonthStatsFromRedis(sellerId, monthStr);
        }
        return getCachedMonthlyStatsFromRedis(sellerId, monthStr);
    }

    private boolean isCurrentMonth(String monthStr) {
        return YearMonth.now().equals(YearMonth.parse(monthStr));
    }

    /**
     * 주문 통계를 계산하는 핵심 메서드
     */
    private OrderStatsResponse calculateStats(Long sellerId, String monthStr) {
        YearMonth month = YearMonth.parse(monthStr);
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, start, end);
        return calculator.calculate(monthStr, orders);
    }

    @Cacheable(
        value = CURRENT_MONTH_CACHE,
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getRedisCurrentMonthStatsKey(#sellerId)"
    )
    public OrderStatsResponse getCachedCurrentMonthStatsFromRedis(Long sellerId, String monthStr) {
        String key = CacheKeyUtil.getRedisCurrentMonthStatsKey(sellerId);
        log.info("[캐시 MISS] Redis 캐시 저장 시도: key={}, sellerId={}, monthStr={}", key, sellerId, monthStr);
        log.info("[캐시 MISS] CURRENT_MONTH_CACHE → sellerId={}, monthStr={}", sellerId, monthStr);
        return calculateStats(sellerId, monthStr);
    }

    @Cacheable(
        value = PAST_MONTH_CACHE,
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getRedisPastMonthStatsKey(#sellerId, #monthStr)"
    )
    public OrderStatsResponse getCachedMonthlyStatsFromRedis(Long sellerId, String monthStr) {
        log.info("[캐시 MISS] PAST_MONTH_CACHE → sellerId={}, monthStr={}", sellerId, monthStr);
        String key = CacheKeyUtil.getRedisPastMonthStatsKey(sellerId, monthStr);
        log.info("Redis 캐시 최종 key 확인: {}", key);
        return calculateStats(sellerId, monthStr);
    }

    @Cacheable(
        value = "sellerStats",
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getLocalMonthlyStatsKey(#sellerId, #monthStr)"
    )
    public OrderStatsResponse getCachedMonthlyStats(Long sellerId, String monthStr) {
        log.info("[캐시 MISS] LOCAL CACHE (sellerStats) → sellerId={}, monthStr={}", sellerId, monthStr);
        return calculateStats(sellerId, monthStr);
    }
}
