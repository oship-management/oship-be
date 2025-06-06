package org.example.oshipserver.domain.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;

public record OrderInfoDto(
    BigDecimal shipmentActualWeight,
    BigDecimal shipmentVolumeWeight,
    CountryCode countryCode) {

    @Builder
    public OrderInfoDto {
    }

}
