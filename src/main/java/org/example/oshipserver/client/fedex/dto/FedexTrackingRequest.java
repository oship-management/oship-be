package org.example.oshipserver.client.fedex.dto;

import java.util.List;

public record FedexTrackingRequest(
        boolean includeDetailedScans,
        List<TrackingInfo> trackingInfo
) {}
