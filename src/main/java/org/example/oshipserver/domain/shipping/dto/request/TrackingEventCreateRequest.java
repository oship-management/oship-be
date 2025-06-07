package org.example.oshipserver.domain.shipping.dto.request;

import jakarta.validation.constraints.NotNull;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

public record TrackingEventCreateRequest(
    @NotNull TrackingEventEnum event,
    String description
) {}
