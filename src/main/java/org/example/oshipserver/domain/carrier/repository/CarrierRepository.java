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
        SELECT new org.example.oshipserver.domain.carrier.dto.CarrierRateDto$Amount(c, cr.amount)
        FROM Carrier c
        JOIN CarrierCountry cc        ON cc.carrier = c
        JOIN CarrierRateCharge cr     ON cr.carrier = c
        WHERE cc.countryCode LIKE :countryCode
            AND c.weightMin < :weight
            AND c.weightMax > :weight
            AND cr.expired > :expired
    """)
    List<CarrierRateDto.Amount> findCarrierAmountDtoByCountryAndWeightAndNotExpired(
        @Param("countryCode") String countryCode,
        @Param("weight") BigDecimal weight,
        @Param("expired") LocalDateTime expired
    );
}
