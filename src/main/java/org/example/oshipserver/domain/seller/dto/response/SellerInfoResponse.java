package org.example.oshipserver.domain.seller.dto.response;

import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.user.enums.UserRole;

import java.time.LocalDateTime;

public record SellerInfoResponse(
        Long sellerId,
        String firstName,
        String lastName,
        String phoneNo,
        String companyName,
        String companyRegisterNo,
        String companyTelNo,
        Long userId,
        String email,
        UserRole userRole,
        LocalDateTime lastLoginAt,
        AuthAddressResponse addressResponse
) {
}
