package org.example.oshipserver.domain.order.service;

import static org.example.oshipserver.global.config.RedisCacheConfig.CURRENT_MONTH_CACHE;
import static org.example.oshipserver.global.config.RedisCacheConfig.PAST_MONTH_CACHE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderStatsService {

    private final OrderRepository orderRepository;

    /**
     * 셀러의 월간 주문 통계를 조회
     *
     * 1. 월 시작일 ~ 말일 범위로 주문 데이터를 조회하고,
     * 2. 해당 데이터를 바탕으로 총 주문 수, 총 중량, 총 금액, 상태별 건수, 일자별 건수를 집계
     *
     * @param sellerId 통계를 조회할 셀러 ID
     * @param monthStr 조회할 기준 월 ("yyyy-MM" 형식)
     * @return 통계 응답 DTO (OrderStatsResponse)
     */
    public OrderStatsResponse getMonthlyStats(Long sellerId, String monthStr) {
        // 입력된 문자열을 기반으로 연월을 파싱
        YearMonth month = YearMonth.parse(monthStr);

        // 해당 월의 시작일과 종료일을 LocalDateTime으로 계산
        LocalDateTime start = month.atDay(1).atStartOfDay();            // ex) 2024-06-01T00:00
        LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX); // ex) 2024-06-30T23:59:59.999...

        // 조건에 맞는 주문 목록 조회 (삭제되지 않은 주문만)
        List<Order> orders = orderRepository.findBySellerIdAndCreatedAtBetween(sellerId, start, end);

        // 결과 집계 후 DTO 생성 및 반환
        return OrderStatsResponse.from(
            monthStr,
            orders.size(),
            getStatusCounts(orders),
            getTotalWeight(orders),
            getTotalAmount(orders),
            getDailyOrderCount(orders)
        );
    }

    /**
     *  [v2] 캐시 적용된 월별 통계 API
     *
     * - 현재 월: 캐시 사용하지 않고 매번 DB 조회
     * - 과거 월: 변동이 없으므로 캐시 적용
     *
     */
    public OrderStatsResponse getMonthlyStatsV2(Long sellerId, String monthStr) {
        if (isCurrentMonth(monthStr)) {
            // 현재 월은 캐싱하지 않고 항상 fresh하게 조회
            return getMonthlyStats(sellerId, monthStr);
        } else {
            // 과거 월은 캐시를 적용하여 조회
            return getCachedMonthlyStats(sellerId, monthStr);
        }
    }


    /**
     * [v3] Redis 캐시 기반 월별 통계 API
     *
     * - 현재 월: 실시간 조회
     * - 과거 월: Redis 캐시 사용
     */
    public OrderStatsResponse getMonthlyStatsV3(Long sellerId, String monthStr) {
        if (isCurrentMonth(monthStr)) {
            return getCachedCurrentMonthStatsFromRedis(sellerId, monthStr);
        }
        else {
            return getCachedMonthlyStatsFromRedis(sellerId, monthStr); // 과거 월은 Redis 캐시 사용
        }
    }

    /**
     * 현재 월 Redis 캐시 적용 (TTL: 5분)
     */
    @Cacheable(
        value = CURRENT_MONTH_CACHE,
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getRedisCurrentMonthStatsKey(#sellerId)"
    )
    public OrderStatsResponse getCachedCurrentMonthStatsFromRedis(Long sellerId, String monthStr) {
        return getMonthlyStats(sellerId, monthStr);
    }

    /**
     * 과거 월 Redis 캐시 적용 (TTL: 7일)
     */
    @Cacheable(
        value = PAST_MONTH_CACHE,
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getRedisPastMonthStatsKey(#sellerId, #monthStr)"
    )
    public OrderStatsResponse getCachedMonthlyStatsFromRedis(Long sellerId, String monthStr) {
        return getMonthlyStats(sellerId, monthStr);
    }


    /**
     * 캐시가 적용된 과거 월 통계 조회
     *
     * - Caffeine 등 로컬 캐시 사용
     */
    @Cacheable(
        value = "sellerStats",
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getLocalMonthlyStatsKey(#sellerId, #monthStr)"
    )
    public OrderStatsResponse getCachedMonthlyStats(Long sellerId, String monthStr) {
        return getMonthlyStats(sellerId, monthStr);
    }

    /**
     *  현재 월인지 판단
     *
     * @param monthStr "yyyy-MM" 형식의 입력 문자열
     * @return true: 현재 월, false: 과거 월
     */
    private boolean isCurrentMonth(String monthStr) {
        YearMonth now = YearMonth.now();
        YearMonth input = YearMonth.parse(monthStr);
        return now.equals(input);
    }

    /**
     * 주문 상태별 건수를 계산
     * 예: PENDING 10건, SHIPPED 8건 등
     *
     * @param orders 주문 리스트
     * @return 상태별 주문 수 집계 Map
     */
    private Map<OrderStatus, Long> getStatusCounts(List<Order> orders) {
        return orders.stream()
            .collect(Collectors.groupingBy(Order::getCurrentStatus, Collectors.counting()));
    }

    /**
     * 전체 주문의 실제 중량 합계를 계산
     * - null 값은 제외
     *
     * @param orders 주문 리스트
     * @return 총 중량 (Kg)
     */
    private BigDecimal getTotalWeight(List<Order> orders) {
        return orders.stream()
            .map(Order::getShipmentActualWeight)
            .filter(weight -> weight != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 전체 주문의 총 금액을 계산
     * - 각 주문의 아이템별 (단가 × 수량)을 합산
     * - 금액 단위는 KRW 기준이라고 가정
     *
     * @param orders 주문 리스트
     * @return 총 금액 (long, 단위: KRW)
     */
    private long getTotalAmount(List<Order> orders) {
        return orders.stream()
            .flatMap(order -> order.getOrderItems().stream())  // 모든 주문의 아이템 평탄화
            .map(item -> item.getUnitValue()  // 단가
                .multiply(BigDecimal.valueOf(item.getQuantity()))) // 단가 × 수량
            .reduce(BigDecimal.ZERO, BigDecimal::add) // 합산
            .longValue(); // 최종 long 변환 (소수점 버림)
    }

    /**
     * 일자별 주문 수를 집계
     * - 주문의 생성 일자(createdAt 기준)를 기준으로 그룹핑
     *
     * @param orders 주문 리스트
     * @return 날짜별 주문 수 Map (예: 2024-06-01 → 5건)
     */
    private Map<LocalDate, Long> getDailyOrderCount(List<Order> orders) {
        return orders.stream()
            .collect(Collectors.groupingBy(
                order -> order.getCreatedAt().toLocalDate(),
                Collectors.counting()
            ));
    }
}
