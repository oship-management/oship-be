package org.example.oshipserver.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.common.response.BaseExceptionResponse;
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
    private final ObjectMapper objectMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String jwt = jwtUtil.extractTokenFromHeader(request);
        if(jwt != null){ //토큰이 있다면?
            try {
                Claims claims = jwtUtil.validateToken(jwt).getBody();
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                String roleStr = claims.get("userRole", String.class);
                UserRole userRole = UserRole.of(roleStr);
                UserDetails userDetails = new CustomUserDetail(userId,email,userRole);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (Exception e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=UTF-8");
                BaseExceptionResponse errorResponse = new BaseExceptionResponse(401, "유효하지 않은 토큰입니다");
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
