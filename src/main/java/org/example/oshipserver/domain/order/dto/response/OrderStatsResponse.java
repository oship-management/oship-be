package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;

public record OrderStatsResponse(
    String month,
    int totalOrders,
    Map<OrderStatus, Long> statusCounts,
    BigDecimal totalWeightKg,
    Amount totalOrderValue,
    Map<String, Long> dailyOrderCount // redis 역직렬화 문제로 변경 LocalDate → String
) {
    public static OrderStatsResponse from(
        String month,
        int totalOrders,
        Map<OrderStatus, Long> statusCounts,
        BigDecimal totalWeightKg,
        long totalAmount,
        Map<LocalDate, Long> dailyOrderCount
    ) {
        return new OrderStatsResponse(
            month,
            totalOrders,
            statusCounts,
            totalWeightKg,
            new Amount(totalAmount, "KRW"),
            convertDateMapToStringMap(dailyOrderCount) // 변환 메서드 적용
        );
    }

    private static Map<String, Long> convertDateMapToStringMap(Map<LocalDate, Long> map) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(), // ex) "2025-06-01"
                Map.Entry::getValue
            ));
    }

    public record Amount(long amount, String currency) {}
}
