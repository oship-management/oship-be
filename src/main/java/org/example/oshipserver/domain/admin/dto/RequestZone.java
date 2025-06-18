package org.example.oshipserver.domain.admin.dto;

import jakarta.validation.constraints.NotNull;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;

public record RequestZone(
    @NotNull(message = "carrierId는 필수입니다.") Long carrierId,
    @NotNull(message = "zoneNo는 필수입니다.") Integer zoneNo,
    @NotNull(message = "countryCode는 필수입니다.") CountryCode countryCode
) {
}
