package org.example.oshipserver.domain.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class RefreshTokenVo {
    private final String refreshToken;
    private final Date expiredAt;
}
