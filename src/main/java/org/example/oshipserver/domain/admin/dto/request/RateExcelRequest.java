package org.example.oshipserver.domain.admin.dto.request;

import java.util.Map;
import lombok.Builder;

@Builder
public record RateExcelRequest(
    Double weight,
    Map<Integer, Double> amounts
) {

}
