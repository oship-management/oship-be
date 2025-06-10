package org.example.oshipserver.global.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.oshipserver.domain.auth.vo.AccessTokenVo;
import org.example.oshipserver.domain.auth.vo.RefreshTokenVo;
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
    private static final long TOKEN_TIME = 1000L * 60 * 15; // 3분
    private static final long REFRESH_TIME = 1000L * 60 * 60 * 12; // 12시간
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

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
        Date expireAt = new Date(now.getTime() + REFRESH_TIME);

        String refreshToken =
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .setIssuedAt(now)
                        .setExpiration(new Date(now.getTime() + REFRESH_TIME))
                        .signWith(key, signatureAlgorithm)
                        .compact();
        return new RefreshTokenVo(refreshToken, expireAt);
    }

    public static void setCookieToken(HttpServletResponse response, AccessTokenVo accessTokenVo){
        long now = System.currentTimeMillis();

        long accessTokenMaxAge = (accessTokenVo.getExpiredAt().getTime() - now) / 1000;

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessTokenVo.getAccessToken())
                .path("/")
                .httpOnly(true)
                .secure(true) // HTTPS 환경일 경우 true
                .sameSite("Strict")
                .maxAge(accessTokenMaxAge)
                .build();


        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    }

    public static void deleteAuthCookies(HttpServletResponse response) {
        ResponseCookie deleteAccessToken = ResponseCookie.from("access_token", "")
                .path("/")
                .maxAge(0)  // 즉시 만료
                .httpOnly(true)
                .build();

        response.addHeader("Set-Cookie", deleteAccessToken.toString());
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

    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken);

            // 만료 여부는 parse 과정에서 자동 확인됨
            return true;
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 오류, 잘못된 형식 등
            return false;
        }
    }

}