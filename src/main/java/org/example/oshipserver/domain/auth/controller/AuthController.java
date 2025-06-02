package org.example.oshipserver.domain.auth.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.enums.AuthSuccessType;
import org.example.oshipserver.domain.auth.service.AuthService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
private final AuthService authService;


private final String uuid = String.valueOf(UUID.randomUUID());

    @GetMapping("/health")
    public String healthCheck() {
        return "OK-SERVER-" + uuid;
    }

    @PostMapping("/sellers/v1/signup")
    public BaseResponse<Long> signup_seller(@RequestBody SellerSignupRequest request){
        Long userId =  authService.signupSeller(request);
        AuthSuccessType authSuccessType = AuthSuccessType.SIGNED_UP;
        return new BaseResponse<>(authSuccessType.getStatus(),authSuccessType.getMessage(), userId);
    }

    @PostMapping("/partners/v1/signup")
    public BaseResponse<Long> signup_partner(@RequestBody PartnerSignupRequest request){
        Long userId =  authService.signupPartner(request);
        AuthSuccessType authSuccessType = AuthSuccessType.SIGNED_UP;
        return new BaseResponse<>(authSuccessType.getStatus(),authSuccessType.getMessage(), userId);
    }

    @PostMapping("/v1/login")
    public BaseResponse<String> login(@RequestBody @Valid LoginRequest request) {
        String response = authService.login(request);
        return new BaseResponse<>(200, "로그인 성공", response);
    }


}
