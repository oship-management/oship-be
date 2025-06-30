
package org.example.oshipserver.domain.shipping.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
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
public class BarcodeService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final CarrierRepository carrierRepository;
    private final PartnerRepository partnerRepository;
    private final TrackingEventHandler trackingEventHandler;

    public Long validateBarcode(String barcode, Authentication authentication) {
        // 권한 검증 - 파트너만 바코드 스캔 가능
        Long userId = Long.valueOf(authentication.getName());
        
        // userId로 Partner 조회
        Partner partner = partnerRepository.findByUserId(userId)
            .orElseThrow(() -> new ApiException("파트너 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
        Long partnerId = partner.getId();

        // 1. 바코드에서 MasterNo 추출
        String masterNo = extractMasterNoFromBarcode(barcode);

        // 2. 박스 번호 검증 (MVP에서는 무조건 1개)
        validateBoxNumber(barcode);

        // 3. 주문 존재 여부 확인
        Order order = orderRepository.findByOshipMasterNo(masterNo)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 4. 바코드 출력 여부 확인
        if (order.getIsPrintBarcode() == null || !order.getIsPrintBarcode()) {
            throw new ApiException("바코드가 출력되지 않았습니다.", ErrorType.BARCODE_NOT_PRINTED);
        }

        // 5. 배송 정보 조회
        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
            .orElseThrow(() -> new ApiException("배송 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
            
        // 6. Carrier 조회하여 파트너 검증
        Carrier carrier = carrierRepository.findById(shipment.getCarrierId())
            .orElseThrow(() -> new ApiException("운송사를 찾을 수 없습니다: " + shipment.getCarrierId(), ErrorType.NOT_FOUND));
            
        // 소유권 검증 - 해당 파트너의 배송물인지 확인
        if (carrier.getPartner() == null || !carrier.getPartner().getId().equals(partnerId)) {
            throw new ApiException("해당 배송물에 대한 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        // 7. AWB 발행 여부 확인
        if (shipment.getAwbUrl() != null && !shipment.getAwbUrl().isEmpty()) {
            throw new ApiException("이미 AWB가 발행된 주문입니다.", ErrorType.AWB_ALREADY_ISSUED);
        }

        // 8. 바코드 스캔 성공 트래킹 이벤트 추가
        trackingEventHandler.handleTrackingEvent(
            order.getId(),
            TrackingEventEnum.CENTER_ARRIVED,
            ""
        );

        return shipment.getId();
    }

    @Transactional
    public void markBarcodePrinted(Long orderId, Authentication authentication) {
        // 권한 검증 - 셀러만 바코드 출력 가능
        Long userId = Long.valueOf(authentication.getName());
        
        // 1. 주문 존재 여부 확인
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다: " + orderId, ErrorType.NOT_FOUND));
            
        // 소유권 검증 - 자신의 주문인지 확인
        if (!order.getSellerId().equals(userId)) {
            throw new ApiException("해당 주문에 대한 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        // 2. 이미 바코드가 생성되었는지 확인
        if (order.getIsPrintBarcode() != null && order.getIsPrintBarcode()) {
            throw new ApiException("이미 바코드가 생성된 주문입니다.", ErrorType.DUPLICATED_ORDER);
        }

        // 3. 바코드 생성 완료 처리
        order.markBarcodeGenerated();

        // 4. 바코드 생성 트래킹 이벤트 추가
        trackingEventHandler.handleTrackingEvent(
            orderId,
            TrackingEventEnum.LABEL_CREATED,
            ""
        );
    }

    private String extractMasterNoFromBarcode(String barcode) {
        // 바코드 형식: OSH250604US1234567/1
        // MasterNo: OSH250604US1234567
        if (barcode == null || !barcode.contains("/")) {
            throw new ApiException("유효하지 않은 바코드 형식입니다.", ErrorType.INVALID_BARCODE_FORMAT);
        }

        return barcode.substring(0, barcode.lastIndexOf("/"));
    }

    private void validateBoxNumber(String barcode) {
        // 바코드 형식: OSH250604US1234567/1
        // MVP에서는 무조건 1개이므로 /1만 허용
        if (!barcode.endsWith("/1")) {
            throw new ApiException("유효하지 않은 박스 번호입니다.", ErrorType.INVALID_BOX_NUMBER);
        }
    }
}