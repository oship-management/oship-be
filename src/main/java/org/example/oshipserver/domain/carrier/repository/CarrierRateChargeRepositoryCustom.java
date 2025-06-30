package org.example.oshipserver.domain.carrier.repository;

import org.example.oshipserver.domain.order.entity.enums.CountryCode;

import java.math.BigDecimal;

public interface CarrierRateChargeRepositoryCustom {
    BigDecimal findChargeByCarrierAndWeightAndCountry(Long carrierId, BigDecimal weight, CountryCode countryCode);
}