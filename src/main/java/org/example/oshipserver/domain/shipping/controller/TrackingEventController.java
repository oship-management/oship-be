package org.example.oshipserver.domain.shipping.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.shipping.dto.request.TrackingEventCreateRequest;
import org.example.oshipserver.domain.shipping.dto.response.TrackingEventCreateResponse;
import org.example.oshipserver.domain.shipping.dto.response.TrackingEventResponse;
import org.example.oshipserver.domain.shipping.service.TrackingEventService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class TrackingEventController {

    private final TrackingEventService trackingEventService;

    @PostMapping("/orders/{orderId}/tracking-events")
    public BaseResponse<TrackingEventCreateResponse> createTrackingEvent(
        @PathVariable("orderId") Long orderId,
        @Valid @RequestBody TrackingEventCreateRequest request) {

        TrackingEventCreateResponse response = trackingEventService.createTrackingEvent(orderId, request);

        return new BaseResponse<>(201, "Tracking event created successfully", response);
    }

    @GetMapping("/orders/{orderId}/tracking-events")
    public BaseResponse<List<TrackingEventResponse>> getTrackingEvents(
        @PathVariable("orderId") Long orderId) {

        List<TrackingEventResponse> events = trackingEventService.getTrackingEvents(orderId);

        return new BaseResponse<>(201, "Tracking events retrieved successfully", events);
    }
}