package org.example.oshipserver.domain.auth.vo;

import lombok.Getter;

import java.util.Date;

@Getter
public class TokenValueObject {
    private final String accessToken;
    private final String refreshToken;
    private final Date accessTokenExpiredAt;
    private final Date refreshTokenExpiredAt;

    public TokenValueObject(AccessTokenVo accessTokenVo, RefreshTokenVo refreshTokenVo){
        this.accessToken = accessTokenVo.getAccessToken();
        this.accessTokenExpiredAt = accessTokenVo.getExpiredAt();
        this.refreshToken = refreshTokenVo.getRefreshToken();
        this.refreshTokenExpiredAt = refreshTokenVo.getExpiredAt();
    }
}
