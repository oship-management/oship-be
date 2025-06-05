package org.example.oshipserver.domain.shipping.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.shipping.dto.response.ShipmentCreateResponse;
import org.example.oshipserver.domain.shipping.service.ShipmentService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/orders/{orderId}/carriers/{carrierId}")
    public BaseResponse<ShipmentCreateResponse> createShipment(
            @PathVariable Long orderId,
            @PathVariable Long carrierId) {

        Long shipmentId = shipmentService.createShipment(orderId, carrierId);
        return new BaseResponse<>(201, "주문배송사연결성공", new ShipmentCreateResponse(shipmentId));
    }
}
