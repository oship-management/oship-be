package org.example.oshipserver.domain.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
@AllArgsConstructor
@Getter
public class AccessTokenVo {
    private final String accessToken;
    private final Date expiredAt;
}
