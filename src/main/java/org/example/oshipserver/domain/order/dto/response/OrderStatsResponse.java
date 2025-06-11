package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;

public record OrderStatsResponse(
    String month,
    int totalOrders,
    Map<OrderStatus, Long> statusCounts,
    BigDecimal totalWeightKg,
    Amount totalOrderValue,
    Map<LocalDate, Long> dailyOrderCount
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
            dailyOrderCount
        );
    }

    public record Amount(long amount, String currency) {}
}
