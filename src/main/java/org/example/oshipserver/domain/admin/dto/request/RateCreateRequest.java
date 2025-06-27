package org.example.oshipserver.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Builder;

@Builder
public record RateCreateRequest(
    @NotNull
    Integer index,
    @NotNull
    BigDecimal weight,
    @NotNull
    Map<Integer, BigDecimal> amounts
) {

}
