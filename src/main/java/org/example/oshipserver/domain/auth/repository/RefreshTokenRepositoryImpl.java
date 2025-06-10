package org.example.oshipserver.domain.auth.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository{

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "token:";

    @Override
    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
