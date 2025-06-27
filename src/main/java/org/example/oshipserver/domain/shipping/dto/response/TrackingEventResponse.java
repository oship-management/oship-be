package org.example.oshipserver.domain.shipping.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

@Builder
public record TrackingEventResponse(
    Long id,
    Long orderId,
    TrackingEventEnum event,
    String eventDescription,  // enum의 desc 값
    String description,       // 사용자 입력 description
    LocalDateTime createdAt
) {}
