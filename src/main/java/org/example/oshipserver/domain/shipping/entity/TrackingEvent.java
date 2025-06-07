package org.example.oshipserver.domain.shipping.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "tracking_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackingEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event", nullable = false, length = 50)
    private TrackingEventEnum event;

    @Column(name = "description")
    private String description;

    @Builder
    private TrackingEvent(Long orderId, TrackingEventEnum event, String description) {
        this.orderId = orderId;
        this.event = event;
        this.description = description;
    }

    // 정적 팩토리 메서드
    public static TrackingEvent createTrackingEvent(Long orderId, TrackingEventEnum event, String description) {
        return TrackingEvent.builder()
            .orderId(orderId)
            .event(event)
            .description(description)
            .build();
    }
}