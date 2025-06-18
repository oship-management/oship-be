package org.example.oshipserver.domain.admin.dto.request;

public record RateExcelRequest(
    int index,
    Long carrierId,
    Integer zoneIndex,
    Double weight,
    Double amount
) {

}
