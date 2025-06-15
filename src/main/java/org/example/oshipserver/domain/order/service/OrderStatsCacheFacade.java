package org.example.oshipserver.domain.order.service;

import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatsCacheFacade {

    private final OrderStatsCacheService cacheService;

    /**
     * v1 - 캐시 없이 항상 fresh하게 계산
     */
    public OrderStatsResponse getMonthlyStats(Long sellerId, String monthStr) {
        return cacheService.getFreshStats(sellerId, monthStr);
    }


    /**
     * v2 - Local 캐시 버전
     */
    public OrderStatsResponse getMonthlyStatsV2(Long sellerId, String monthStr) {
        return isCurrentMonth(monthStr)
            ? cacheService.getFreshStats(sellerId, monthStr)
            : cacheService.getCachedMonthlyStatsFromLocal(sellerId, monthStr);
    }

    /**
     * v3 - Redis 캐시 버전
     */
    public OrderStatsResponse getMonthlyStatsV3(Long sellerId, String monthStr) {
        return isCurrentMonth(monthStr)
            ? cacheService.getCachedCurrentMonthStatsFromRedis(sellerId, monthStr)
            : cacheService.getCachedMonthlyStatsFromRedis(sellerId, monthStr);
    }

    private boolean isCurrentMonth(String monthStr) {
        return YearMonth.now().equals(YearMonth.parse(monthStr));
    }
}
