package org.example.oshipserver.domain.order.service.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStatsCalculator {

    public OrderStatsResponse calculate(String monthStr, List<Order> orders) {
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
            .flatMap(order -> order.getOrderItems().stream())  // 모든 주문의 아이템 평탄화 -> 여기서 N+1 발생
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
