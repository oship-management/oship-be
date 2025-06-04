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
    public BaseResponse<Long> signup_seller(@RequestBody @Valid SellerSignupRequest request){
        Long userId =  authService.signupSeller(request);
        return new BaseResponse<>(201,"회원가입 성공", userId);
    }

    @PostMapping("/partners/signup")
    public BaseResponse<Long> signup_partner(@RequestBody @Valid PartnerSignupRequest request){
        Long userId =  authService.signupPartner(request);
        return new BaseResponse<>(201,"회원가입 성공", userId);
    }

    @PostMapping("/login")
    public BaseResponse<TokenValueObject> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletResponse response) {
        TokenValueObject token = authService.login(request);
        setCookieToken(response, token);
        return new BaseResponse<>(201, "로그인 성공", token);
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(
            HttpServletResponse response){
        deleteAuthCookies(response);
        return new BaseResponse<>(204, "로그아웃 성공", null);
    }


}
