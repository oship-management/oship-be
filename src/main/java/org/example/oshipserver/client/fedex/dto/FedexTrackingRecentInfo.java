package org.example.oshipserver.client.fedex.dto;

public record FedexTrackingRecentInfo(
        String trackingNumber,
        String date,
        String derivedStatusCode
) {
}
