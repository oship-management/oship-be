package org.example.oshipserver.domain.shipping.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.shipping.dto.request.TrackingEventCreateRequest;
import org.example.oshipserver.domain.shipping.dto.response.TrackingEventCreateResponse;
import org.example.oshipserver.domain.shipping.dto.response.TrackingEventResponse;
import org.example.oshipserver.domain.shipping.entity.TrackingEvent;
import org.example.oshipserver.domain.shipping.repository.TrackingEventRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackingEventService {

    private final TrackingEventRepository trackingEventRepository;
    private final OrderRepository orderRepository;

    public TrackingEventCreateResponse createTrackingEvent(Long orderId, TrackingEventCreateRequest request) {
        // 1. 주문 존재 여부 확인
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다: " + orderId, ErrorType.NOT_FOUND));

        // 2. 트래킹 이벤트 생성
        TrackingEvent trackingEvent = TrackingEvent.createTrackingEvent(
            orderId,
            request.event(),
            request.description()
        );

        // 3. 저장
        TrackingEvent savedEvent = trackingEventRepository.save(trackingEvent);

        // 4. Order에 마지막 트래킹 이벤트 업데이트
        order.updateLastTrackingEvent(request.event().name());

        // 5. 응답 생성
        return TrackingEventCreateResponse.builder()
            .id(savedEvent.getId())
            .orderId(savedEvent.getOrderId())
            .event(savedEvent.getEvent())
            .description(savedEvent.getDescription())
            .createdAt(savedEvent.getCreatedAt())
            .build();
    }

    @Transactional(readOnly = true)
    public List<TrackingEventResponse> getTrackingEvents(Long orderId) {
        // 1. 주문 존재 여부 확인
        orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다: " + orderId, ErrorType.NOT_FOUND));

        // 2. 트래킹 이벤트 조회 (생성일시 역순)
        List<TrackingEvent> events = trackingEventRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

        // 3. 응답 DTO 변환
        return events.stream()
            .map(event -> TrackingEventResponse.builder()
                .id(event.getId())
                .orderId(event.getOrderId())
                .event(event.getEvent())
                .eventDescription(event.getEvent().getDesc())  // enum의 desc 값
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .build())
            .toList();
    }
}