package org.example.oshipserver.domain.partner.dto.response;

import org.example.oshipserver.domain.user.enums.UserRole;

import java.time.LocalDateTime;

public record PartnerInfoResponse (
        Long partnerId,
        String companyName,
        String companyTelNo,
        String companyRegisterNo,
        Long userId,
        String email,
        UserRole userRole,
        LocalDateTime lastLoginAt
){
}
