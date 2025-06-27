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
        WITH order_country AS (
             SELECT DISTINCT
               o.id                         AS orderId,
               ra.recipient_country_code    AS countryCode,
               GREATEST(o.shipment_actual_weight, o.shipment_volume_weight) AS rawWeight
             FROM orders o
             JOIN order_recipients ore ON ore.order_id = o.id
             JOIN recipient_addresses ra ON ra.id = ore.recipient_address_id
             WHERE o.id IN (:orderIds)
           )
           SELECT
             p.id                        AS partnerId,
             p.company_name              AS partnerName,
             c.id                        AS carrierId,
             c.name                      AS carrierName,
             SUM(cr.amount)              AS totalAmount,
             JSON_OBJECTAGG(oc.orderId, cr.amount) AS orderAmountMap
           FROM order_country oc
           JOIN carrier_countries cc
             ON cc.country_code = oc.countryCode
           JOIN carriers c
             ON c.id = cc.carrier_id
           JOIN partners p
             ON p.id = c.partner_id
           JOIN LATERAL (
             SELECT amount
             FROM carrier_rate_charges cr2
             WHERE cr2.carrier_id = c.id
               AND cr2.weight = CEIL(oc.rawWeight * 2) / 2
               AND CEIL(oc.rawWeight * 2) / 2 BETWEEN c.weight_min AND c.weight_max
               AND cr2.deleted_at IS NULL
             LIMIT 1
           ) cr ON TRUE
           WHERE c.expired > NOW()
           GROUP BY p.id, p.company_name, c.id, c.name
           ORDER BY p.company_name;
        """, nativeQuery = true)
    List<PartnerCarrierNativeDto> findPartnerCarrierNative(
        @Param("orderIds") List<Long> orderIds
    );
}
