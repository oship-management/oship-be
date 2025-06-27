package org.example.oshipserver.client.fedex.dto;


public record TrackingInfo(
        String shipDateBegin,
        TrackingNumberInfo trackingNumberInfo
) {}