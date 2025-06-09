package org.example.oshipserver.domain.shipping.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.shipping.dto.response.BarcodeValidationResponse;
import org.example.oshipserver.domain.shipping.service.BarcodeService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
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
        @RequestParam("barcode") String barcode) {

        Long shipmentId = barcodeService.validateBarcode(barcode);

        return new BaseResponse<>(201, "처리가능합니다. 무게정보를 입력해주세요.",
            new BarcodeValidationResponse(shipmentId));
    }
}