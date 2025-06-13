package org.example.oshipserver.domain.seller.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.seller.dto.request.SellerDeleteRequest;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.service.SellerService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping
    public ResponseEntity<BaseResponse<SellerInfoResponse>> getSellerInfo(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        SellerInfoResponse info = sellerService.getSellerInfo(userId);
        BaseResponse<SellerInfoResponse> response = new BaseResponse<>(200, "조회 성공", info);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("/withdraw")
    public ResponseEntity<BaseResponse<Object>> deleteSeller(
            Authentication authentication,
            @RequestBody @Valid SellerDeleteRequest request){
        Long userId = Long.valueOf(authentication.getName());
        sellerService.deleteSeller(userId, request, authentication.getCredentials().toString());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/addresses")
    public ResponseEntity<BaseResponse<AuthAddressResponse>> updateAddress(
            @RequestBody AuthAddressRequest request,
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        AuthAddressResponse addressResponse = sellerService.updateAddress(userId, request);
        BaseResponse<AuthAddressResponse> response = new BaseResponse<>(200, "주소 수정 성공", addressResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
