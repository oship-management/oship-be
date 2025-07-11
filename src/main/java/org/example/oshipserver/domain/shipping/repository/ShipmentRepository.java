package org.example.oshipserver.domain.shipping.repository;

import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    boolean existsByOrderId(Long orderId);
    Optional<Shipment> findByOrderId(Long orderId);

    Slice<Shipment> findAllByCarrierId(Long carrierId, Pageable pageable);
    //배송추적위한것
    Slice<Shipment> findAllByCarrierIdAndDeliveredAtIsNull(Long carrierId, Pageable pageable);
}
