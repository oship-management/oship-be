package org.example.oshipserver.domain.carrier.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.carrier.dto.CarrierRateDto.getResponse;
import org.example.oshipserver.domain.carrier.service.CarrierService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/carriers")
@RestController
public class CarrierController {

    private final CarrierService carrierService;

    @GetMapping("/rates/{orderId}")
    public ResponseEntity<BaseResponse<List<getResponse>>> getRates(
        @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(HttpStatus.OK.value(), "성공", carrierService.getRates(orderId)));
    }
}
