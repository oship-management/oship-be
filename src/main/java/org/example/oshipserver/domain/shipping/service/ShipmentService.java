package org.example.oshipserver.domain.shipping.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.client.fedex.FedexClient;
import org.example.oshipserver.client.fedex.FedexShipmentResponse;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRateChargeRepository;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.domain.shipping.dto.request.ShipmentMeasureRequest;
import org.example.oshipserver.domain.shipping.dto.response.AwbResponse;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.example.oshipserver.domain.shipping.service.interfaces.TrackingEventHandler;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final CarrierRepository carrierRepository;
    private final CarrierRateChargeRepository carrierRateChargeRepository;
    private final OrderRepository orderRepository;
    private final PartnerRepository partnerRepository;
    private final TrackingEventHandler trackingEventHandler;
    private final FedexClient fedexClient;

    public Long createShipment(Long orderId, Long carrierId, Authentication authentication) {
        // 권한 검증 - 셀러만 배송 연결 가능
        Long userId = Long.valueOf(authentication.getName());
        
        // 주문 존재 여부 확인
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다: " + orderId, ErrorType.NOT_FOUND));
            
        // 소유권 검증 - 자신의 주문인지 확인
        if (!order.getSellerId().equals(userId)) {
            throw new ApiException("해당 주문에 대한 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        // Carrier 조회
        Carrier carrier = carrierRepository.findById(carrierId)
            .orElseThrow(() -> new ApiException("운송사를 찾을 수 없습니다: " + carrierId, ErrorType.NOT_FOUND));

        // 이미 배송이 생성되어 있는지 확인
        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new ApiException("이미 배송이 생성된 주문입니다: " + orderId, ErrorType.DUPLICATED_ORDER);
        }

        // Carrier의 Partner ID를 Order에 설정
        if (carrier.getPartner() != null) {
            order.assignPartner(carrier.getPartner().getId());
            orderRepository.save(order);
        }

        // 배송 생성
        Shipment shipment = Shipment.createShipment(orderId, carrierId);

        // 저장
        Shipment savedShipment = shipmentRepository.save(shipment);

        return savedShipment.getId();
    }

    public AwbResponse updateMeasureAndGenerateAwb(Long shipmentId, ShipmentMeasureRequest request, Authentication authentication) {
        // 권한 검증 - 파트너만 AWB 발급 가능
        Long userId = Long.valueOf(authentication.getName());
        
        // userId로 Partner 조회
        Partner partner = partnerRepository.findByUserId(userId)
            .orElseThrow(() -> new ApiException("파트너 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
        Long partnerId = partner.getId();
        
        // 1. 배송 정보 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ApiException("배송 정보를 찾을 수 없습니다: " + shipmentId, ErrorType.NOT_FOUND));
            
        // Carrier 조회하여 파트너 검증
        Carrier carrier = carrierRepository.findById(shipment.getCarrierId())
            .orElseThrow(() -> new ApiException("운송사를 찾을 수 없습니다: " + shipment.getCarrierId(), ErrorType.NOT_FOUND));
            
        // 소유권 검증 - 해당 파트너의 배송물인지 확인
        if (carrier.getPartner() == null || !carrier.getPartner().getId().equals(partnerId)) {
            throw new ApiException("해당 배송에 대한 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        // 2. 주문 정보 조회 (MasterNo 획득을 위해)
        Order order = orderRepository.findById(shipment.getOrderId())
            .orElseThrow(() -> new ApiException("주문 정보를 찾을 수 없습니다: " + shipment.getOrderId(), ErrorType.NOT_FOUND));

        // 3. 이미 AWB가 발행되었는지 확인
        if (shipment.getAwbUrl() != null && !shipment.getAwbUrl().isEmpty()) {
            throw new ApiException("이미 AWB가 발행된 배송입니다.", ErrorType.AWB_ALREADY_ISSUED);
        }

        // 4. 부피 무게 계산 (가로 × 세로 × 높이) / 5000
        BigDecimal volumeWeight = calculateVolumeWeight(request.width(), request.height(), request.length());
        
        // 5. 측정 정보 업데이트 (부피 무게 포함)
        shipment.updateMeasurements(
            request.width(),
            request.height(),
            request.length(),
            request.grossWeight()
        );
        shipment.updateVolumeWeight(volumeWeight);
        
        // 6. 요금 계산
        BigDecimal chargeValue = calculateShippingCharge(shipment, order);
        shipment.updateChargeValue(chargeValue);

        // 7. AWB URL 생성
        FedexShipmentResponse fedexResponse = fedexClient.requestAwbLabelUrl(shipment, order, request);
        shipment.updateAwb(fedexResponse.labelUrl(), fedexResponse.carrierTrackingNo());

        // 8. 저장
        shipmentRepository.save(shipment);

        // 9. AWB 생성 트래킹 이벤트 추가
        trackingEventHandler.handleTrackingEvent(
            order.getId(),
            TrackingEventEnum.AWB_CREATED,
            ""
        );

        // 10. OrderTable AWB 생성 완료 처리
        order.markAwbGenerated();

        // 11. 응답 데이터 생성 (Builder 패턴 사용)
        AwbResponse.MeasurementData measurements = AwbResponse.MeasurementData.builder()
            .width(request.width())
            .height(request.height())
            .length(request.length())
            .grossWeight(request.grossWeight())
            .build();

        AwbResponse.ShipmentData shipmentData = AwbResponse.ShipmentData.builder()
            .shipmentId(shipmentId)
            .masterNo(order.getOshipMasterNo())
            .url(fedexResponse.labelUrl())
            .measurements(measurements)
            .build();

        return AwbResponse.builder()
            .shipment(shipmentData)
            .build();
    }
    
    /**
     * 부피 무게 계산: (가로 × 세로 × 높이) / 5000
     */
    private BigDecimal calculateVolumeWeight(BigDecimal width, BigDecimal height, BigDecimal length) {
        if (width == null || height == null || length == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal volume = width.multiply(height).multiply(length);
        return volume.divide(new BigDecimal("5000"), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 배송 요금 계산
     */
    private BigDecimal calculateShippingCharge(Shipment shipment, Order order) {
        // 실제 무게와 부피 무게 중 큰 값 사용
        BigDecimal actualWeight = shipment.getGrossWeight() != null ? shipment.getGrossWeight() : BigDecimal.ZERO;
        BigDecimal volumeWeight = shipment.getVolumeWeight() != null ? shipment.getVolumeWeight() : BigDecimal.ZERO;
        BigDecimal chargeableWeight = actualWeight.compareTo(volumeWeight) > 0 ? actualWeight : volumeWeight;
        
        // 0.5kg 단위로 올림 (20kg 미만인 경우)
        if (chargeableWeight.compareTo(new BigDecimal("20")) < 0) {
            BigDecimal halfKg = new BigDecimal("0.5");
            chargeableWeight = chargeableWeight.divide(halfKg, 0, RoundingMode.UP).multiply(halfKg);
        }
        
        // QueryDSL로 요금 조회
        return carrierRateChargeRepository.findChargeByCarrierAndWeightAndCountry(
            shipment.getCarrierId(),
            chargeableWeight,
            order.getRecipient().getRecipientAddress().getRecipientCountryCode()
        );
    }
}