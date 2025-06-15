package org.example.oshipserver.domain.carrier.repository;

import java.util.List;
import org.example.oshipserver.domain.carrier.dto.PartnerCarrierNativeDto;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    @Query(value = """
            SELECT
              p.id                     AS partnerId,
              p.company_name           AS partnerName,
              c.id                     AS carrierId,
              c.name                   AS carrierName,
              SUM(cr.amount)           AS totalAmount,
              JSON_OBJECTAGG(o.id, cr.amount) AS orderAmountMap
            FROM orders o
            JOIN order_recipients ore  ON ore.order_id = o.id
            JOIN recipient_addresses ra  ON ra.id = ore.recipient_address_id
            JOIN carrier_countries cc    ON cc.country_code = ra.recipient_country_code
            JOIN carriers c              ON c.id = cc.carrier_id
            JOIN partners p              ON p.id = c.partner_id
            JOIN carrier_rate_charges cr
              ON cr.carrier_id = c.id
              AND cr.weight = GREATEST(o.shipment_actual_weight, o.shipment_volume_weight)
              AND c.expired > NOW()
            WHERE o.id IN (:orderIds)
              AND GREATEST(o.shipment_actual_weight, o.shipment_volume_weight)
                  BETWEEN c.weight_min AND c.weight_max
            GROUP BY p.id, c.id
            ORDER BY p.company_name;
           """, nativeQuery = true)
    List<PartnerCarrierNativeDto> findPartnerCarrierNative(
        @Param("orderIds") List<Long> orderIds
    );
}
