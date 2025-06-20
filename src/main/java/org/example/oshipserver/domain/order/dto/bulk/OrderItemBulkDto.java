package org.example.oshipserver.domain.order.dto.bulk;

import java.time.LocalDateTime;

public record OrderItemBulkDto(
    String name,
    Integer quantity,
    Double unitValue,
    String valueCurrency,
    Double weight,
    String hsCode,
    String originCountryCode,
    String originStateCode,
    String originStateName,
    String weightUnit,
    Long orderId,// FK (orders.id 참조)
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {}
