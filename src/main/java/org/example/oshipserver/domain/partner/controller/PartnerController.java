package org.example.oshipserver.domain.partner.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.service.PartnerService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.example.oshipserver.global.common.utils.JwtUtil.deleteAuthCookies;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;


    @GetMapping
    public BaseResponse<PartnerInfoResponse> getPartnerInfo(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        PartnerInfoResponse response = partnerService.getPartnerInfo(userId);
        return new BaseResponse<>(200, "조회 성공", response);
    }

    @PostMapping("/withdraw")
    public BaseResponse<Void> deletePartner(
            Authentication authentication,
            @RequestBody @Valid PartnerDeleteRequest request,
            HttpServletResponse response
    ){
        Long userId = Long.valueOf(authentication.getName());
        partnerService.deletePartner(userId, request);
        deleteAuthCookies(response);
        return new BaseResponse<>(204, "파트너 삭제 성공", null);
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


}
