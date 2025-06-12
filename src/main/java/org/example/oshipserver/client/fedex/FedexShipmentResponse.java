package org.example.oshipserver.client.fedex;

import lombok.Builder;

public record FedexShipmentResponse(
    String carrierTrackingNo,
    String labelUrl,
    String shipDate
) {
    @Builder
    public FedexShipmentResponse {}
}

