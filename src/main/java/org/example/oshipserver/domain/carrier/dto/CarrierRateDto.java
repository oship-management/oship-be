package org.example.oshipserver.domain.carrier.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import org.example.oshipserver.domain.carrier.entity.Carrier;

public class CarrierRateDto {

//    @Builder
//    public static record getResponse(
//        double kg,
//        String countryCode,
//        Partner partner
//    ) { }
//
//    @Builder
//    public static record Partner(
//        Long partnerId,
//        String partnerName,
//        Long carrierId,
//        String carrierName,
//        BigDecimal amount
//    ) { }

    /**
     * 파트너별 응답
     */
    @Builder
    public record PartnerResponse(
        Long partnerId,
        String partnerName,
        BigDecimal totalAmount,
        List<CarrierResponse> carriers
    ) { }

    /**
     * 운송사별 응답
     */
    @Builder
    public record CarrierResponse(
        Long carrierId,
        String carrierName,
        BigDecimal totalAmount,
        List<OrderResponse> orders
    ) { }

    /**
     * 주문별 상세 정보
     */
    @Builder
    public record OrderResponse(
        Long orderId,
        double kg,
        String countryCode,
        BigDecimal amount
    ) { }

    public static record Amount(
        Carrier carrier,
        BigDecimal amount,
        Long id
    ) {

    }

}
