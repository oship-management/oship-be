package org.example.oshipserver.domain.order.service.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderStatsCalculator {

    public OrderStatsResponse calculate(String monthStr, List<Order> orders) {
        return OrderStatsResponse.from(
            monthStr,
            orders.size(),
            getStatusCounts(orders),
            getTotalWeight(orders),
            getDailyOrderCount(orders)
        );
    }

    /**
     * lastTrackingEvent를 사용자 친화 배송 상태로 매핑 후 카운트
     */
    private Map<String, Long> getStatusCounts(List<Order> orders) {
        return orders.stream()
            .map(order -> mapTrackingEventToShippingStatus(order.getLastTrackingEvent()))
            .filter(status -> status != null) // null(매핑 불가)은 제외
            .collect(Collectors.groupingBy(status -> status, Collectors.counting()));
    }

    /**
     * lastTrackingEvent를 사용자 친화 배송 상태로 변환
     */
    private String mapTrackingEventToShippingStatus(String event) {
        if (event == null) return "ETC";

        return switch (event) {
            case "ORDER_PLACED" -> "PREPARING";
            case "LABEL_CREATED" -> "READY";
            case "AWB_CREATED", "CENTER_ARRIVED", "PICKUP_DELAY", "READY_SHIP", "SHIPPED",
                 "DEST_COUNTRY_ARRIVED", "CLEARANCE_DELAY", "IN_CLEARANCE", "CLEARED",
                 "FINAL_DEST_TRANSIT", "FINAL_DEST_ARRIVED", "IN_TRANSIT", "HUB_ARRIVED",
                 "DELIVERY_DELAY", "DELIVERY_EXCEPTION", "IN_DELIVERY" -> "SHIPPING";
            case "DELIVERED" -> "DELIVERED";
            case "RETURN" -> "RETURNING";
            default -> "ETC";
        };
    }

    /**
     * 전체 주문의 실제 중량 합계
     */
    private BigDecimal getTotalWeight(List<Order> orders) {
        return orders.stream()
            .map(Order::getShipmentActualWeight)
            .filter(weight -> weight != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
