package org.example.oshipserver.domain.order.dto.request;

public record OrderItemRequest(
    Long id,
    String itemName,
    int itemQuantity,
    double itemUnitValue,
    String itemValueCurrency,
    double itemWeight,
    String weightUnit,
    String itemHSCode,
    String itemOriginCountryCode
) {}
