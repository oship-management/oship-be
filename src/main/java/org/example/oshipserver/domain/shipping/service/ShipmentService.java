package org.example.oshipserver.domain.shipping.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.shipping.dto.request.ShipmentMeasureRequest;
import org.example.oshipserver.domain.shipping.dto.response.AwbResponse;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public Long createShipment(Long orderId, Long carrierId) {
        // 주문 존재 여부 확인
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다: " + orderId, ErrorType.NOT_FOUND));

        // 이미 배송이 생성되어 있는지 확인
        if (shipmentRepository.existsByOrderId(orderId)) {
            throw new ApiException("이미 배송이 생성된 주문입니다: " + orderId, ErrorType.DUPLICATED_ORDER);
        }

        // 배송 생성
        Shipment shipment = Shipment.createShipment(orderId, carrierId);

        // 저장
        Shipment savedShipment = shipmentRepository.save(shipment);

        return savedShipment.getId();
    }

    public AwbResponse updateMeasureAndGenerateAwb(Long shipmentId, ShipmentMeasureRequest request) {
        // 1. 배송 정보 조회
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ApiException("배송 정보를 찾을 수 없습니다: " + shipmentId, ErrorType.NOT_FOUND));

        // 2. 주문 정보 조회 (MasterNo 획득을 위해)
        Order order = orderRepository.findById(shipment.getOrderId())
            .orElseThrow(() -> new ApiException("주문 정보를 찾을 수 없습니다: " + shipment.getOrderId(), ErrorType.NOT_FOUND));

        // 3. 이미 AWB가 발행되었는지 확인
        if (shipment.getAwbUrl() != null && !shipment.getAwbUrl().isEmpty()) {
            throw new ApiException("이미 AWB가 발행된 배송입니다.", ErrorType.AWB_ALREADY_ISSUED);
        }

        // 4. 측정 정보 업데이트
        shipment.updateMeasurements(
            BigDecimal.valueOf(request.width()),
            BigDecimal.valueOf(request.height()),
            BigDecimal.valueOf(request.length()),
            request.grossWeight()
        );

        // 5. AWB URL 생성 (하드코딩)
        String awbUrl = "FEDEX_LABEL_URL";
        shipment.updateAwbUrl(awbUrl);

        // 6. 저장
        shipmentRepository.save(shipment);

        // 7. 응답 데이터 생성 (Builder 패턴 사용)
        AwbResponse.MeasurementData measurements = AwbResponse.MeasurementData.builder()
            .width(request.width())
            .height(request.height())
            .length(request.length())
            .grossWeight(request.grossWeight())
            .build();

        AwbResponse.ShipmentData shipmentData = AwbResponse.ShipmentData.builder()
            .shipmentId(shipmentId)
            .masterNo(order.getOshipMasterNo())
            .url(awbUrl)
            .measurements(measurements)
            .build();

        return AwbResponse.builder()
            .shipment(shipmentData)
            .build();
    }
}
