package org.example.oshipserver.domain.order.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import org.example.oshipserver.domain.order.entity.OrderItem;

public record OrderItemRequest(
    Long id,
    @NotBlank String itemName,
    @Min(1) int itemQuantity,
    @DecimalMin("0.0") double itemUnitValue,
    @NotBlank String itemValueCurrency,
    @DecimalMin("0.0") double itemWeight,
    @NotBlank String weightUnit,
    @NotBlank String itemHSCode,
    @NotBlank String itemOriginCountryCode
) {
    public OrderItem toEntity() {
        return OrderItem.builder()
            .name(itemName)
            .quantity(itemQuantity)
            .unitValue(BigDecimal.valueOf(itemUnitValue))
            .valueCurrency(itemValueCurrency)
            .weight(BigDecimal.valueOf(itemWeight))
            .weightUnit(weightUnit)
            .hsCode(itemHSCode)
            .originCountryCode(itemOriginCountryCode)
            .build();
    }
}
