package org.example.oshipserver.domain.partner.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.service.PartnerService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
