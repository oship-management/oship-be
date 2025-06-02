package org.example.oshipserver.domain.user.enums;

import java.util.Arrays;
import org.example.oshipserver.domain.auth.enums.AuthErrorType;
import org.example.oshipserver.global.exception.ApiException;

public enum UserRole {
    SELLER,
    PARTNER,
    ADMIN;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                   .filter(r -> r.name().equalsIgnoreCase(role))
                   .findFirst()
                   .orElseThrow(() -> new ApiException("유효하지 않은 UerRole", AuthErrorType.INVALID_USER_ROLE));
    }
}
