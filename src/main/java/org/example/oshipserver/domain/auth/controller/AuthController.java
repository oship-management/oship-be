package org.example.oshipserver.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.service.AuthService;
import org.example.oshipserver.domain.auth.vo.TokenValueObject;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.example.oshipserver.global.common.utils.JwtUtil.deleteAuthCookies;
import static org.example.oshipserver.global.common.utils.JwtUtil.setCookieToken;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
private final AuthService authService;


private final String uuid = String.valueOf(UUID.randomUUID());

    @GetMapping("/health")
    public String healthCheck(Authentication authentication) {
        System.out.println(authentication.getName());
        return "OK-SERVER-" + uuid;
    }

    @PostMapping("/sellers/v1/signup")
    public BaseResponse<Long> signup_seller(@RequestBody SellerSignupRequest request){
        Long userId =  authService.signupSeller(request);
        return new BaseResponse<>(201,"회원가입 성공", userId);
    }

    @PostMapping("/partners/v1/signup")
    public BaseResponse<Long> signup_partner(@RequestBody PartnerSignupRequest request){
        Long userId =  authService.signupPartner(request);
        return new BaseResponse<>(201,"회원가입 성공", userId);
    }

    @PostMapping("/v1/login")
    public BaseResponse<TokenValueObject> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        TokenValueObject token = authService.login(request);
        setCookieToken(response, token);
        return new BaseResponse<>(201, "로그인 성공", token);
    }

    @PostMapping("/v1/logout")
    public BaseResponse<Void> logout(
            HttpServletResponse response){
        deleteAuthCookies(response);
        return new BaseResponse<>(204, "로그아웃 성공", null);
    }


}
