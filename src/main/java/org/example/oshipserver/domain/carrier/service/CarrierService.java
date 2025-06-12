package org.example.oshipserver.domain.carrier.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.dto.CarrierRateDto;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.dto.OrderRateResponseDto;
import org.example.oshipserver.domain.order.service.OrderRateService;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final OrderRateService orderRateService;
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
        OrderRateResponseDto orderInfo,
        BigDecimal amount
    ) {

    }

    public List<CarrierRateDto.CarrierResponse> getCarrierRatesForOrder(List<Long> orderIds) {

        // 1) orderInfos 조회
        List<OrderRateResponseDto> orderInfos = orderRateService.getOrderInfos(orderIds);

        // 2) Flat 리스트 생성
        List<Flat> flats = orderInfos.stream()
            .flatMap(orderInfo -> {
                String country = orderInfo.countryCode().toString();
                BigDecimal weight = orderInfo.weight();
                LocalDateTime now = LocalDateTime.now();

                // 2-1) 해당 주문 무게·국가로 운송사 조회
                List<Carrier> carriers = carrierRepository
                    .findCarrierByCountryAndWeight(country, now, weight);

                if(carriers.isEmpty()){
                    throw new ApiException("운송 가능한 운송사가 없습니다.", ErrorType.INVALID_PARAMETER);
                }

                // 2-2) 운송사별 요금 조회 및 Flat 매핑
                return carriers.stream().flatMap(carrier -> {
                    List<CarrierRateDto.Amount> amounts = carrierRepository
                        .findCarrierAmountDtoByCarrierIdAndWeightAndNotExpired(
                            weight, carrier.getId()
                        );

                    if(amounts.isEmpty()){
                        throw new ApiException("운송 가능한 운송사가 없습니다.", ErrorType.INVALID_PARAMETER);
                    }

                    return amounts.stream()
                        .map(crAmt -> new Flat(
                            carrier.getPartner().getId(),
                            carrier.getPartner().getCompanyName(),
                            carrier.getId(),
                            carrier.getName().toString(),
                            orderInfo,
                            crAmt.amount()
                        ));
                });
            })
            .toList();

        // 3) carrierId 기준 그룹핑 → CarrierResponse 생성
        Map<Long, List<Flat>> byCarrier = flats.stream()
            .collect(Collectors.groupingBy(Flat::carrierId));

        return byCarrier.entrySet().stream()
            .map(carEntry -> {
                Long carrierId     = carEntry.getKey();
                List<Flat> cList   = carEntry.getValue();
                String carrierName = cList.get(0).carrierName();
                Long   partnerId   = cList.get(0).partnerId();
                String partnerName = cList.get(0).partnerName();

                // 3-1) orderId별로 중복 제거 및 합산
                Map<Long, List<Flat>> byOrder = cList.stream()
                    .collect(Collectors.groupingBy(f -> f.orderInfo().orderId()));

                List<CarrierRateDto.OrderResponse> orders = byOrder.entrySet().stream()
                    .map(orderEntry -> {
                        Long orderId    = orderEntry.getKey();
                        OrderRateResponseDto info = orderEntry.getValue().get(0).orderInfo();
                        // 주문 내 여러 금액이 있다면 합산
                        BigDecimal orderTotal = orderEntry.getValue().stream()
                            .map(Flat::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                        return new CarrierRateDto.OrderResponse(
                            orderId,
                            info.weight().doubleValue(),
                            info.countryCode().toString(),
                            orderTotal
                        );
                    })
                    .toList();

                // 3-2) Carrier 전체 합계
                BigDecimal carrierTotal = orders.stream()
                    .map(CarrierRateDto.OrderResponse::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return new CarrierRateDto.CarrierResponse(
                    carrierId,
                    carrierName,
                    partnerId,
                    partnerName,
                    carrierTotal,
                    orders
                );
            })
            .toList();
    }
}
