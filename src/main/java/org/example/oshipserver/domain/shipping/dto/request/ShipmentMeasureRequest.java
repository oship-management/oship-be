
package org.example.oshipserver.domain.shipping.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record ShipmentMeasureRequest(
    @Min(1) int width,
    @Min(1) int height,
    @Min(1) int length,
    @DecimalMin("0.1") BigDecimal grossWeight
) {}