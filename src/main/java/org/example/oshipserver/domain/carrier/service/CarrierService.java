package org.example.oshipserver.domain.carrier.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.dto.CarrierRateDto;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.dto.OrderInfoDto;
import org.example.oshipserver.domain.order.service.OrderInfoFacade;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final OrderInfoFacade orderInfoFacade;
    private final CarrierRepository carrierRepository;

    /*
     * 참조하는 다른 entity
     * Order: getShipmentActualWeight, getShipmentVolumeWeight
     * RecipientAddress: recipientCountryCode
     *
     * 무게 측정
     * actualWeight(실제 무게), volumeWeight(부피 무게)를 비교
     * 더 큰 값을 기준으로 0.5kg 단위로 올림(20kg 미만인 경우)
     *
     * carriers를 가져오는 기준
     * 1. weight가 min~max 사이에 있는지
     * 2. 요금 만료일자가 지나지 않았는지
     * 3. 운송 가능한 곳인지
     *
     */
    public List<CarrierRateDto.getResponse> getRates(Long orderId) {
        OrderInfoDto orderInfo = orderInfoFacade.getOrderInfo(orderId);

        BigDecimal weight = orderInfo.shipmentActualWeight().compareTo(orderInfo.shipmentVolumeWeight()) > 0 ? orderInfo.shipmentActualWeight() : orderInfo.shipmentVolumeWeight();

        List<CarrierRateDto.Amount> carriers = carrierRepository.findCarrierAmountDtoByCountryAndWeightAndNotExpired(orderInfo.countryCode().toString(), weight, LocalDateTime.now());

        List<CarrierRateDto.getResponse> responses = carriers.stream()
            .map(amount -> {
                CarrierRateDto.Partner partner = CarrierRateDto.Partner.builder()
                    .partnerId(amount.carrier().getPartner().getId())
                    .partnerName(amount.carrier().getPartner().getCompanyName())
                    .carrierId(amount.carrier().getId())
                    .carrierName(amount.carrier().getName().toString())
                    .amount(amount.amount())
                    .build();

                return CarrierRateDto.getResponse.builder()
                    .kg(weight.intValue())
                    .countryCode(orderInfo.countryCode().toString())
                    .partner(partner)
                    .build();
            })
            .toList();

        return responses;
    }
}
