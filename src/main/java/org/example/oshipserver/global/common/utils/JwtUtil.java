package org.example.oshipserver.global.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.example.oshipserver.domain.auth.vo.RefreshTokenVo;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final long TOKEN_TIME = 1000L * 60 * 15; // 3분
    private static final long REFRESH_TIME = 1000L * 60 * 60 * 12; // 12시간

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    //accesstoken 발급
    public String createToken(Long userId, String email, UserRole userRole) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + TOKEN_TIME);

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("userRole", userRole)
                .setExpiration(expireAt)
                .setIssuedAt(now)
                .signWith(key, signatureAlgorithm)
                .compact();
        return accessToken;
    }

    //refreshtoken 발급
    public RefreshTokenVo createRefreshToken(Long userId) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + REFRESH_TIME);

        String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(key, signatureAlgorithm).compact();
        return new RefreshTokenVo(refreshToken, expireAt);
    }

    public String extractTokenFromHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public Claims validateToken(String token) {
        try {
            // 토큰 파싱 및 검증 (서명 확인, 만료일 확인 포함)
            return Jwts.parserBuilder()
                    .setSigningKey(key) // key는 JWT 생성 시 사용한 secret key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰 만료
            throw new ApiException("Access token expired", ErrorType.TOKEN_EXPIRED);
        } catch (SignatureException e) {
            // 서명 오류 (위조된 토큰)
            throw new ApiException("Invalid token signature", ErrorType.UNAUTHORIZED);
        } catch (JwtException e) {
            // 그 외 JWT 관련 오류
            throw new ApiException("Invalid token", ErrorType.UNAUTHORIZED);
        }
    }

    //리프레시 토큰 검증
    public boolean isRefreshTokenValid(String refreshToken) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken);
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