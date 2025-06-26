package org.example.oshipserver.domain.shipping.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.oshipserver.client.fedex.enums.TrackingEventEnum;

public record TrackingEventCreateRequest(
    @NotNull TrackingEventEnum event,
    String description
) {}
