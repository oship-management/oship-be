package org.example.oshipserver.domain.seller.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.seller.dto.request.SellerDeleteRequest;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.service.SellerService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static org.example.oshipserver.global.common.utils.JwtUtil.deleteAuthCookies;

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
    @PostMapping("/withdraw")
    public BaseResponse<Void> deleteSeller(
            Authentication authentication,
            @RequestBody @Valid SellerDeleteRequest request,
            HttpServletResponse response
    ){
        Long userId = Long.valueOf(authentication.getName());
        sellerService.deleteSeller(userId, request);
        deleteAuthCookies(response);
        return new BaseResponse<>(204, "셀러 삭제 성공", null);
    }
}
