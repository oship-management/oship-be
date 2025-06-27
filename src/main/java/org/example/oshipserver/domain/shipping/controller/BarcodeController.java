package org.example.oshipserver.domain.shipping.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.shipping.dto.response.BarcodePrintResponse;
import org.example.oshipserver.domain.shipping.dto.response.BarcodeValidationResponse;
import org.example.oshipserver.domain.shipping.service.BarcodeService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class BarcodeController {

    private final BarcodeService barcodeService;

    @GetMapping("/barcode")
    public BaseResponse<BarcodeValidationResponse> validateBarcode(
        @RequestParam("barcode") String barcode,
        Authentication authentication) {

        Long shipmentId = barcodeService.validateBarcode(barcode, authentication);

        return new BaseResponse<>(201, "처리가능합니다. 무게정보를 입력해주세요.",
            new BarcodeValidationResponse(shipmentId));
    }

    @PatchMapping("/orders/{orderId}/barcode-printed")
    public BaseResponse<BarcodePrintResponse> markBarcodePrinted(
        @PathVariable("orderId") Long orderId,
        Authentication authentication) {

        barcodeService.markBarcodePrinted(orderId, authentication);

        return new BaseResponse<>(201, "바코드 프린트 상태가 업데이트되었습니다.",
            new BarcodePrintResponse(orderId, true));
    }
}