package org.example.oshipserver.domain.shipping.repository;

import java.util.List;
import org.example.oshipserver.domain.shipping.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    List<TrackingEvent> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
