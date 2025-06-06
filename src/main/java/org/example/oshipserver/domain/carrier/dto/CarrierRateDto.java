package org.example.oshipserver.domain.carrier.dto;

import java.math.BigDecimal;
import lombok.Builder;
import org.example.oshipserver.domain.carrier.entity.Carrier;

public class CarrierRateDto {

    @Builder
    public static record getResponse(
        int kg,
        String countryCode,
        Partner partner
    ) { }

    @Builder
    public static record Partner(
        Long partnerId,
        String partnerName,
        Long carrierId,
        String carrierName,
        BigDecimal amount
    ) { }


    public static record Amount(
       Carrier carrier,
       BigDecimal amount
    ){ }


}
