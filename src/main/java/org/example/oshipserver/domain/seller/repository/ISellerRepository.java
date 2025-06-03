package org.example.oshipserver.domain.seller.repository;

import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;

import java.util.Optional;

public interface ISellerRepository {
    Optional<SellerInfoResponse> findSellerInfoByUserId(Long userId);
}
