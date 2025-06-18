package org.example.oshipserver.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record RateCreateRequest(
    @NotNull
    Long carrierId,
    @NotNull
    Integer zoneIndex,
    @NotNull
    BigDecimal weight,
    @NotNull
    BigDecimal amount
) {

}
