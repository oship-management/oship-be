package org.example.oshipserver.domain.shipping.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.shipping.dto.request.ShipmentMeasureRequest;
import org.example.oshipserver.domain.shipping.dto.response.AwbResponse;
import org.example.oshipserver.domain.shipping.dto.response.ShipmentCreateResponse;
import org.example.oshipserver.domain.shipping.service.ShipmentService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping("/orders/{orderId}/carriers/{carrierId}")
    public BaseResponse<ShipmentCreateResponse> createShipment(
        @PathVariable("orderId") Long orderId,
        @PathVariable("carrierId") Long carrierId,
        Authentication authentication) {

        Long shipmentId = shipmentService.createShipment(orderId, carrierId, authentication);
        return new BaseResponse<>(201, "주문배송사연결성공", new ShipmentCreateResponse(shipmentId));
    }

    @PatchMapping("/shipment/{shipmentId}")
    public BaseResponse<AwbResponse> updateShipmentMeasure(
        @PathVariable("shipmentId") Long shipmentId,
        @Valid @RequestBody ShipmentMeasureRequest request,
        Authentication authentication) {

        AwbResponse response = shipmentService.updateMeasureAndGenerateAwb(shipmentId, request, authentication);

        return new BaseResponse<>(201, "AWB발급성공", response);
    }

}
