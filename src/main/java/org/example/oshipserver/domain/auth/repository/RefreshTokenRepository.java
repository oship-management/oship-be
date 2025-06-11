package org.example.oshipserver.domain.auth.repository;

public interface RefreshTokenRepository{
    void saveRefreshToken(Long userId, String refreshToken, long expirationMillis);
    String getRefreshToken(Long userId);
    void deleteRefreshToken(Long userId);
    void addBlackList(String accessToken);
    public boolean isBlacklisted(String accessToken);
}
