package org.example.oshipserver.global.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.common.utils.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String jwt = jwtUtil.extractTokenFromCookies(request, "access_token");
        if(jwt != null){ //토큰이 있다면?
            if(!jwtUtil.validateToken(jwt)){
                //토큰 유효한지? 만료기간등
                //유효하지 않으면 리프레시 토큰확인하고 토큰 재발급
                filterChain.doFilter(request, response);
                return;
            }
            Claims claims = jwtUtil.extractClaims(jwt);
            String userId = jwtUtil.extractClaims(jwt).getSubject();
            String email = claims.get("email", String.class);
            String roleStr = claims.get("userRole", String.class);
            UserRole userRole = UserRole.of(roleStr);
            UserDetails userDetails = new CustomUserDetail(userId,email,userRole);
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
