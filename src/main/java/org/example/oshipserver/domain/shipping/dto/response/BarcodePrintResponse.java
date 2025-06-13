package org.example.oshipserver.domain.shipping.dto.response;

public record BarcodePrintResponse(
    Long orderId,
    boolean barcodeGenerated
) {}
