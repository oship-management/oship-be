package org.example.oshipserver.domain.shipping.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ShipmentMeasureRequest(
    @DecimalMin("1") BigDecimal width,
    @DecimalMin("1") BigDecimal height,
    @DecimalMin("1") BigDecimal length,
    @DecimalMin("0.1") BigDecimal grossWeight
) {}
