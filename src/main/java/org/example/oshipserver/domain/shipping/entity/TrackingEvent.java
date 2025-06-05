package org.example.oshipserver.domain.shipping.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "tracking_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)  // enum을 문자열로 저장
    @Column(name = "event", nullable = false, length = 50)
    private TrackingEventEnum event;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
