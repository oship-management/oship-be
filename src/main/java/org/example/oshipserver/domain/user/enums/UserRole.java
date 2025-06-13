package org.example.oshipserver.domain.user.enums;

import java.util.Arrays;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;

public enum UserRole {
    SELLER,
    PARTNER,
    ADMIN;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                   .filter(r -> r.name().equalsIgnoreCase(role))
                   .findFirst()
                   .orElseThrow(() -> new ApiException("유효하지 않은 UerRole", ErrorType.VALID_FAIL));
    }
}
