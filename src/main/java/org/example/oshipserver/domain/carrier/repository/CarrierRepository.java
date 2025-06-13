package org.example.oshipserver.domain.carrier.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oshipserver.domain.carrier.dto.CarrierRateDto;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {

    @Query("""
        SELECT c
        FROM Carrier c
        JOIN CarrierCountry cc        ON cc.carrier = c
        WHERE c.weightMin <= :weight
            AND c.weightMax >= :weight
            AND c.expired > :now
            AND cc.countryCode = :countryCode
    """)
    List<Carrier> findCarrierByCountryAndWeight(
        @Param("countryCode") String countryCode,
        @Param("now") LocalDateTime now,
        @Param("weight") BigDecimal weight
    );

    @Query("""
        SELECT new org.example.oshipserver.domain.carrier.dto.CarrierRateDto$Amount(c, cr.amount, cr.id)
        FROM Carrier c
        JOIN CarrierRateCharge cr        ON cr.carrier = c
        WHERE cr.weight = :weight
            AND c.id = :carrierId
    """)
    List<CarrierRateDto.Amount> findCarrierAmountDtoByCarrierIdAndWeightAndNotExpired(
        @Param("weight") BigDecimal weight,
        @Param("carrierId") Long carrierId
    );
}
