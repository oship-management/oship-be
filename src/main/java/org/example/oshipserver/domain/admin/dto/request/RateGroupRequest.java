package org.example.oshipserver.domain.admin.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record RateGroupRequest(
    Long carrierId,
    Integer zoneIndex,
    List<amounts> amounts
) {

    public static record amounts(
        BigDecimal weight,
        BigDecimal amount
    ) {

    }
}
