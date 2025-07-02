package org.example.oshipserver.domain.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

public record OrderStatsResponse(
    String month,
    int totalOrders,
    Map<String, Long> statusCounts,
    BigDecimal totalWeightKg,
    Map<String, Long> dailyOrderCount
) {
    public static OrderStatsResponse from(
        String month,
        int totalOrders,
        Map<String, Long> statusCounts,
        BigDecimal totalWeightKg,
        Map<LocalDate, Long> dailyOrderCount
    ) {
        return new OrderStatsResponse(
            month,
            totalOrders,
            statusCounts,
            totalWeightKg,
            convertDateMapToStringMap(dailyOrderCount)
        );
    }

    private static Map<String, Long> convertDateMapToStringMap(Map<LocalDate, Long> map) {
        return map.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(), // ex) "2025-06-01"
                Map.Entry::getValue
            ));
    }
}
