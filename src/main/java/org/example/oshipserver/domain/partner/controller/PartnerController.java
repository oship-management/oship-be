package org.example.oshipserver.domain.partner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.service.PartnerService;
import org.example.oshipserver.domain.carrier.dto.response.CarrierListResponse;
import org.example.oshipserver.global.common.response.BaseResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;


    @GetMapping
    public ResponseEntity<BaseResponse<PartnerInfoResponse>> getPartnerInfo(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        PartnerInfoResponse info = partnerService.getPartnerInfo(userId);
        BaseResponse<PartnerInfoResponse> response = new BaseResponse<>(200, "조회 성공", info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<Object>> deletePartner(
            Authentication authentication,
            @RequestBody @Valid PartnerDeleteRequest request){
        Long userId = Long.valueOf(authentication.getName());
        partnerService.deletePartner(userId, request,authentication.getPrincipal().toString());
        BaseResponse<Object> deleteResponse= new BaseResponse<>(204, "파트너 삭제 성공", null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(deleteResponse);
    }

    @PutMapping("/addresses")
    public ResponseEntity<BaseResponse<AuthAddressResponse>> updateAddress(
            @RequestBody AuthAddressRequest request,
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        AuthAddressResponse addressResponse = partnerService.updateAddress(userId, request);
        BaseResponse<AuthAddressResponse> response = new BaseResponse<>(200, "주소 수정 성공", addressResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/carriers")
    public ResponseEntity<BaseResponse<List<CarrierListResponse>>> getPartnerCarriers(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        List<CarrierListResponse> carriers = partnerService.getPartnerCarriers(userId);
        BaseResponse<List<CarrierListResponse>> response = new BaseResponse<>(200, "배송사 목록 조회 성공", carriers);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
