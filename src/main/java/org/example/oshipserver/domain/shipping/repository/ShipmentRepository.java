package org.example.oshipserver.domain.shipping.repository;

import java.util.Optional;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<Shipment> findByOrderId(Long orderId);
}
