package org.example.oshipserver.domain.carrier.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.dto.CarrierRateDto;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.dto.OrderInfoDto;
import org.example.oshipserver.domain.order.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final OrderInfoService orderInfoService;
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
     * 운송사별로 처리 가능한 order를 보여줌
     *
     */
    @Builder
    private static record Flat(
        Long partnerId,
        String partnerName,
        Long carrierId,
        String carrierName,
        OrderInfoDto orderInfo,
        BigDecimal amount
    ) {

    }

    public List<CarrierRateDto.PartnerResponse> getCarrierRatesForOrder(List<Long> orderIds) {

        List<OrderInfoDto> orderInfos = orderInfoService.getOrderInfos(orderIds);

        List<Flat> flats = orderInfos.stream()
            .flatMap(orderInfo -> {
                // orderInfo 하나에 대해 이 주문을 처리 가능한 carriers를 조회
                return carrierRepository
                    .findCarrierAmountDtoByCountryAndWeightAndNotExpired(
                        orderInfo.countryCode().toString(),
                        orderInfo.weight(),
                        LocalDateTime.now()
                    )
                    .stream()
                    .map(carAmt -> new Flat(
                        carAmt.carrier().getPartner().getId(),
                        carAmt.carrier().getPartner().getCompanyName(),
                        carAmt.carrier().getId(),
                        carAmt.carrier().getName().toString(),
                        orderInfo,
                        carAmt.amount().multiply(orderInfo.weight())
                    ));
            })
            .toList();

        // 2) partnerId 기준으로 그룹핑
        Map<Long, List<Flat>> byPartner = flats.stream()
            .collect(Collectors.groupingBy(Flat::partnerId));

        // 3) 파트너별 DTO 생성
        List<CarrierRateDto.PartnerResponse> result = byPartner.entrySet().stream()
            .map(partnerEntry -> {
                Long partnerId   = partnerEntry.getKey();
                List<Flat> pList = partnerEntry.getValue();
                String partnerName = pList.get(0).partnerName();

                // 3-1) 이 파트너 안에서 carrierId별로 그룹핑
                Map<Long, List<Flat>> byCarrier = pList.stream()
                    .collect(Collectors.groupingBy(Flat::carrierId));

                // 3-2) CarrierRateDto.CarrierResponse DTO 생성
                List<CarrierRateDto.CarrierResponse> carrierDtos = byCarrier.entrySet().stream()
                    .map(carEntry -> {
                        Long carrierId   = carEntry.getKey();
                        List<Flat> cList = carEntry.getValue();
                        String carrierName = cList.get(0).carrierName();

                        // 주문별로 CarrierRateDto.OrderResponse 모으기
                        List<CarrierRateDto.OrderResponse> orders = cList.stream()
                            .map(f -> new CarrierRateDto.OrderResponse(
                                f.orderInfo().orderId(),
                                f.orderInfo().weight().doubleValue(),
                                f.orderInfo().countryCode().toString(),
                                f.amount()
                            ))
                            .toList();

                        // 이 carrier 전체 금액 합계 계산
                        BigDecimal carrierTotal = orders.stream()
                            .map(CarrierRateDto.OrderResponse::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                        return new CarrierRateDto.CarrierResponse(carrierId, carrierName, carrierTotal, orders);
                    })
                    .toList();

                // 3-3) 파트너 전체 금액 합계 계산
                BigDecimal partnerTotal = carrierDtos.stream()
                    .map(CarrierRateDto.CarrierResponse::totalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new CarrierRateDto.PartnerResponse(partnerId, partnerName, partnerTotal, carrierDtos);
            })
            .toList();

        return result;
    }
}
