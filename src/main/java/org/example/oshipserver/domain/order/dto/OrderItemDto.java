package org.example.oshipserver.domain.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderItemDto(
    @NotBlank String itemName,
    @Min(1) int itemQuantity,
    @DecimalMin("0.0") double itemUnitValue,
    @NotBlank String itemValueCurrency,
    @DecimalMin("0.0") double itemWeight,
    @NotBlank String weightUnit,
    @NotBlank String itemHSCode,
    @NotBlank String itemOriginCountryCode,
    @NotBlank String itemOriginStateCode,
    @NotBlank String itemOriginStateName
) {}

