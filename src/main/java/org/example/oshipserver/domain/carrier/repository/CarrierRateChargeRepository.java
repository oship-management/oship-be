package org.example.oshipserver.domain.carrier.repository;

import org.example.oshipserver.domain.carrier.entity.CarrierRateCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRateChargeRepository extends JpaRepository<CarrierRateCharge, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
    UPDATE CarrierRateCharge c
       SET c.deletedAt = NOW()
     WHERE c.carrier.id   = :carrierId
       AND c.zoneIndex   = :zoneIndex
       AND c.deletedAt IS NULL
  """)
    void softDeleteByCarrierIdAndZoneAndDeletedAtIsNull(
        @Param("carrierId") Long carrierId,
        @Param("zoneIndex") int zoneIndex)
        ;
}
