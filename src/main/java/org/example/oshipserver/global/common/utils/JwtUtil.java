package org.example.oshipserver.global.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

private static final String BEARER_PREFIX = "Bearer ";
private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

@Value("${jwt.secret.key}")
private String secretKey;
private Key key;
private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

@PostConstruct
public void init() {
    byte[] bytes = Base64.getDecoder().decode(secretKey);
    key = Keys.hmacShaKeyFor(bytes);
}

public String createToken(Long userId, String email, UserRole userRole) {
    Date date = new Date();

    return BEARER_PREFIX +
               Jwts.builder()
                   .setSubject(String.valueOf(userId))
                   .claim("email", email)
                   .claim("userRole", userRole)
                   .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                   .setIssuedAt(date)
                   .signWith(key, signatureAlgorithm)
                   .compact();
}

public String substringToken(String tokenValue) {
    if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
        return tokenValue.substring(7);
    }
    throw new ApiException("Not Found Token", ErrorType.NOT_FOUND);
}

public Claims extractClaims(String token) {
    return Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();
}
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token); // 유효성 검사됨 (signature, 만료일 등)
        return true;
    } catch (Exception e) {
        return false;
    }
}

}