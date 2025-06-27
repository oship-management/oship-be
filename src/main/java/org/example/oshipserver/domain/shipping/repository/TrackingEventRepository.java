package org.example.oshipserver.domain.shipping.repository;

import org.example.oshipserver.domain.shipping.entity.TrackingEvent;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    boolean existsByOrderIdAndEvent(Long orderId, TrackingEventEnum event);
}
