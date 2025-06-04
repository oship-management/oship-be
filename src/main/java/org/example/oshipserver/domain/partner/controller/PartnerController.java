package org.example.oshipserver.domain.partner.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.service.PartnerService;
import org.example.oshipserver.global.common.response.BaseResponse;
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
            @RequestBody PartnerDeleteRequest request,
            HttpServletResponse response
    ){
        Long userId = Long.valueOf(authentication.getName());
        partnerService.deletePartner(userId, request);
        deleteAuthCookies(response);
        return new BaseResponse<>(204, "파트너 삭제 성공", null);
    }

}
