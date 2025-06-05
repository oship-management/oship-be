package org.example.oshipserver.global.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oshipserver.domain.auth.vo.AccessTokenVo;
import org.example.oshipserver.domain.auth.vo.RefreshTokenVo;
import org.example.oshipserver.domain.auth.vo.TokenValueObject;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분
    private static final long REFRESH_TIME = 1000L * 60 * 60 * 24 * 3; //3일
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";


    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public AccessTokenVo createToken(Long userId, String email, UserRole userRole) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + TOKEN_TIME);

        String accessToken =
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email)
                        .claim("userRole", userRole)
                        .setExpiration(new Date(now.getTime() + TOKEN_TIME))
                        .setIssuedAt(now)
                        .signWith(key, signatureAlgorithm)
                        .compact();
        return new AccessTokenVo(accessToken, expireAt);
    }

    public RefreshTokenVo createRefreshToken(Long userId) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + TOKEN_TIME);

        String refreshToken =
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .setIssuedAt(now)
                        .setExpiration(new Date(now.getTime() + REFRESH_TIME))
                        .signWith(key, signatureAlgorithm)
                        .compact();
        return new RefreshTokenVo(refreshToken, expireAt);
    }

    public static void setCookieToken(HttpServletResponse response, TokenValueObject token){
        long now = System.currentTimeMillis();

        long accessTokenMaxAge = (token.getAccessTokenExpiredAt().getTime() - now) / 1000;
        long refreshTokenMaxAge = (token.getRefreshTokenExpiredAt().getTime() - now) / 1000;

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token.getAccessToken())
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS 환경일 경우 true
                .sameSite("Strict") // 또는 "Lax" / "None"
                .maxAge(accessTokenMaxAge)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token.getRefreshToken())
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(refreshTokenMaxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    public static void deleteAuthCookies(HttpServletResponse response) {
        ResponseCookie deleteAccessToken = ResponseCookie.from("access_token", "")
                .path("/")
                .maxAge(0)  // 즉시 만료
                .httpOnly(true)
                .build();

        ResponseCookie deleteRefreshToken = ResponseCookie.from("refresh_token", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        response.addHeader("Set-Cookie", deleteAccessToken.toString());
        response.addHeader("Set-Cookie", deleteRefreshToken.toString());
    }

    public String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Jwt<JwsHeader, Claims> validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

}