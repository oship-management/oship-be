package org.example.oshipserver.domain.auth.dto.response;

public record TokenResponse (
        String accessToken,
        String refreshToken
){
}
