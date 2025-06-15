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
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            //만료된 토큰
            throw new ApiException(ErrorType.TOKEN_EXPIRED.getDesc(), ErrorType.TOKEN_EXPIRED);
        } catch (SignatureException e) {
            //서명 오류 (변조 가능성)
            throw new ApiException(ErrorType.TOKEN_SIGNATURE_INVALID.getDesc(), ErrorType.TOKEN_SIGNATURE_INVALID);

        } catch (MalformedJwtException e) {
            //토큰 구조 이상
            throw new ApiException( ErrorType.TOKEN_MALFORMED.getDesc(), ErrorType.TOKEN_MALFORMED);

        } catch (UnsupportedJwtException e) {
            //지원하지 않는 토큰
            throw new ApiException(ErrorType.TOKEN_UNSUPPORTED.getDesc(), ErrorType.TOKEN_UNSUPPORTED);

        } catch (IllegalArgumentException e) {
            //토큰이 null 또는 빈 값
            throw new ApiException(ErrorType.TOKEN_ILLEGAL_ARGUMENT.getDesc(), ErrorType.TOKEN_ILLEGAL_ARGUMENT);

        } catch (JwtException e) {
            //기타 JWT 관련 오류
            throw new ApiException(ErrorType.UNAUTHORIZED.getDesc(), ErrorType.UNAUTHORIZED);
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

    public Claims getClaims(String accessToken){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 토큰은 만료되었지만, Claims는 추출 가능
            return e.getClaims();
        } catch (JwtException e) {
            // 서명 불일치나 기타 파싱 실패 등
            throw new ApiException("유효하지 않은 토큰입니다.", ErrorType.UNAUTHORIZED);
        }
    }

}