package org.example.oshipserver.domain.seller.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;

    public SellerInfoResponse getSellerInfo(Long userId){
        SellerInfoResponse response = sellerRepository.findSellerInfoByUserId(userId)
                .orElseThrow(()->new ApiException("셀러 조회 실패", ErrorType.NOT_FOUND));
        return response;
    }

}
