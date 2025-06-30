package org.example.oshipserver.domain.carrier.dto.response;

import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.enums.CarrierName;
import org.example.oshipserver.domain.carrier.enums.Services;

public record CarrierListResponse(
    Long id,
    CarrierName name,
    String description,
    Services service
) {
    public static CarrierListResponse from(Carrier carrier) {
        return new CarrierListResponse(
                carrier.getId(),
                carrier.getName(),
                carrier.getDescription(),
                carrier.getService()
        );
    }
}