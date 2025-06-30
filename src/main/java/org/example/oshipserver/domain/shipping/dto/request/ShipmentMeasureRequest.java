package org.example.oshipserver.domain.shipping.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ShipmentMeasureRequest(
    @NotNull(message = "Width is required")
    @DecimalMin(value = "1", message = "Width must be at least 1cm")
    @DecimalMax(value = "120", message = "Width must not exceed 120cm")
    BigDecimal width,
    
    @NotNull(message = "Height is required")
    @DecimalMin(value = "1", message = "Height must be at least 1cm")
    @DecimalMax(value = "120", message = "Height must not exceed 120cm")
    BigDecimal height,
    
    @NotNull(message = "Length is required")
    @DecimalMin(value = "1", message = "Length must be at least 1cm")
    @DecimalMax(value = "120", message = "Length must not exceed 120cm")
    BigDecimal length,
    
    @NotNull(message = "Gross weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1kg")
    @DecimalMax(value = "9999", message = "Weight must not exceed 9999kg")
    BigDecimal grossWeight
) {}
