package org.example.oshipserver.domain.shipping.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

@Builder
public record TrackingEventCreateResponse(
    Long id,
    Long orderId,
    TrackingEventEnum event,
    String description,
    LocalDateTime createdAt
) {}