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
}
