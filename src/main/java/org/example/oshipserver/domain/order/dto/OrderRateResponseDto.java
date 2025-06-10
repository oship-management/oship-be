package org.example.oshipserver.domain.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;

public record OrderRateResponseDto(
    Long orderId,
    BigDecimal weight,
    CountryCode countryCode) {

    @Builder
    public OrderRateResponseDto {
    }

}
