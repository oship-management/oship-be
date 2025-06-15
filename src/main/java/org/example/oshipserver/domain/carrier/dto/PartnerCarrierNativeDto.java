package org.example.oshipserver.domain.carrier.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.math.BigDecimal;

public record PartnerCarrierNativeDto(
    Long partnerId,
    String partnerName,
    Long carrierId,
    String carrierName,
    BigDecimal totalAmount,
    @JsonRawValue
    String orderAmountMap
) {

}
