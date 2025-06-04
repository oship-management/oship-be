package org.example.oshipserver.domain.partner.repository;

import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;

import java.util.Optional;

public interface IPartnerRepository {
    Optional<PartnerInfoResponse> findPartnerInfoByUserId(Long userId);
}
