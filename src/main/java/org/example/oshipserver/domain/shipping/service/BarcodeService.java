
package org.example.oshipserver.domain.shipping.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;

    public Long validateBarcode(String barcode) {
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

        // 6. AWB 발행 여부 확인
        if (shipment.getAwbUrl() != null && !shipment.getAwbUrl().isEmpty()) {
            throw new ApiException("이미 AWB가 발행된 주문입니다.", ErrorType.AWB_ALREADY_ISSUED);
        }

        return shipment.getId();
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