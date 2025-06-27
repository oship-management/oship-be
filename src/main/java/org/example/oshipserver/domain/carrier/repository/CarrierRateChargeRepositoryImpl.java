package org.example.oshipserver.domain.carrier.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.entity.QCarrier;
import org.example.oshipserver.domain.carrier.entity.QCarrierCountry;
import org.example.oshipserver.domain.carrier.entity.QCarrierRateCharge;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CarrierRateChargeRepositoryImpl implements CarrierRateChargeRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public BigDecimal findChargeByCarrierAndWeightAndCountry(Long carrierId, BigDecimal weight, CountryCode countryCode) {
        QCarrierRateCharge carrierRateCharge = QCarrierRateCharge.carrierRateCharge;
        QCarrier carrier = QCarrier.carrier;
        QCarrierCountry carrierCountry = QCarrierCountry.carrierCountry;
        
        // 먼저 해당 carrier와 country에 대한 zoneNo를 조회
        Integer zoneNo = queryFactory
            .select(carrierCountry.zoneNo)
            .from(carrierCountry)
            .where(
                carrierCountry.carrier.id.eq(carrierId),
                carrierCountry.countryCode.eq(countryCode)
            )
            .fetchOne();
        
        if (zoneNo == null) {
            return BigDecimal.ZERO;
        }
        
        // zoneNo를 사용하여 요금 조회
        BigDecimal amount = queryFactory
            .select(carrierRateCharge.amount)
            .from(carrierRateCharge)
            .join(carrierRateCharge.carrier, carrier)
            .where(
                carrierRateCharge.carrier.id.eq(carrierId),
                carrierRateCharge.weight.eq(weight),
                carrierRateCharge.zoneIndex.eq(zoneNo),
                carrierRateCharge.deletedAt.isNull(),
                carrier.expired.after(LocalDateTime.now())
            )
            .fetchOne();
        
        return amount != null ? amount : BigDecimal.ZERO;
    }
}