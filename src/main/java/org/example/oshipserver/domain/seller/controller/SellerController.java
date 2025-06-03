package org.example.oshipserver.domain.seller.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.service.SellerService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @GetMapping
    public BaseResponse<SellerInfoResponse> getSellerInfo(
            Authentication authentication
    ){
        Long userId = Long.valueOf(authentication.getName());
        SellerInfoResponse response = sellerService.getSellerInfo(userId);
        return new BaseResponse<>(200, "조회 성공", response);
    }
}
