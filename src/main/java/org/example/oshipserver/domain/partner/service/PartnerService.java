package org.example.oshipserver.domain.partner.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerInfoResponse getPartnerInfo(Long userId) {

        PartnerInfoResponse response = partnerRepository.findPartnerInfoByUserId(userId)
                .orElseThrow(() -> new ApiException("파트너 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
        return response;
    }
}
