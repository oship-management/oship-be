package org.example.oshipserver.domain.shipping.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.shipping.dto.request.TrackingEventCreateRequest;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.example.oshipserver.domain.shipping.service.interfaces.TrackingEventHandler;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingEventHandlerImpl implements TrackingEventHandler {

    private final TrackingEventService trackingEventService;

    @Override
    public void handleTrackingEvent(Long orderId, TrackingEventEnum event, String description) {
        log.info("Creating tracking event - orderId: {}, event: {}, description: {}",
            orderId, event, description);

        // 기존 createTrackingEvent 로직을 그대로 재사용
        TrackingEventCreateRequest request = new TrackingEventCreateRequest(event, description);
        trackingEventService.createTrackingEvent(orderId, request);
    }
}