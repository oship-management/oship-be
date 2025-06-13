package org.example.oshipserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.dto.response.TokenResponse;
import org.example.oshipserver.domain.auth.service.AuthService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
private final AuthService authService;


private final String uuid = String.valueOf(UUID.randomUUID());

    @GetMapping("/health")
    public String healthCheck() {
        return "OK-SERVER-" + uuid;
    }

    @PostMapping("/sellers/signup")
    public ResponseEntity<BaseResponse<Long>> signup_seller(@RequestBody @Valid SellerSignupRequest request){
        Long userId =  authService.signupSeller(request);
        BaseResponse<Long> response = new BaseResponse<>(201,"회원가입 성공", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/partners/signup")
    public ResponseEntity<BaseResponse<Long>> signup_partner(@RequestBody @Valid PartnerSignupRequest request){
        Long userId =  authService.signupPartner(request);
        BaseResponse<Long> response = new BaseResponse<>(201,"회원가입 성공", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request) {
        TokenResponse accessToken = authService.login(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(201, "로그인 성공", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(Authentication authentication){
        Long userId = Long.valueOf(authentication.getName());
        authService.logout(userId,authentication.getCredentials().toString());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new BaseResponse<>(204, "로그아웃 성공", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refreshToken(HttpServletRequest request){
        //레디스를 통해서 알아서 발급을 해줘야하나?..?
        TokenResponse accessToken = authService.refreshToken(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(201, "토큰 재발급", accessToken));
    }



}
