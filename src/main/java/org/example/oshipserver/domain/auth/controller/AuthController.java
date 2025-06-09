package org.example.oshipserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.service.AuthService;
import org.example.oshipserver.domain.auth.vo.AccessTokenVo;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.example.oshipserver.global.common.utils.JwtUtil.deleteAuthCookies;
import static org.example.oshipserver.global.common.utils.JwtUtil.setCookieToken;

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
    public ResponseEntity<AccessTokenVo> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        AccessTokenVo token = authService.login(request);
        setCookieToken(response, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response,
            Authentication authentication){
        Long userId = Long.valueOf(authentication.getName());
        deleteAuthCookies(response);
        authService.logout(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
