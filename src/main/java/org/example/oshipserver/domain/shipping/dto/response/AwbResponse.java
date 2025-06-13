package org.example.oshipserver.domain.shipping.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record AwbResponse(
    ShipmentData shipment
) {
    @Builder
    public record ShipmentData(
        Long shipmentId,
        String masterNo,
        String url,
        MeasurementData measurements
    ) {}

    @Builder
    public record MeasurementData(
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal grossWeight
    ) {}
}