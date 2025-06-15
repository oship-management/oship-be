package org.example.oshipserver.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.common.response.BaseExceptionResponse;
import org.example.oshipserver.global.common.utils.JwtUtil;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String jwt = jwtUtil.extractTokenFromHeader(request);
        if(request.getRequestURI().equals("/api/v1/auth/refresh")){
            filterChain.doFilter(request, response);
            return;
        }
        if(jwt != null && jwt.startsWith("Bearer ")){ //토큰이 있다면?

            try {
                jwt = jwt.substring(7);
                if(refreshTokenRepository.isBlacklisted(jwt)){ //블랙리스트에 토큰 유무 검사 true -> 토큰유효하지 않음
                    throw new ApiException(ErrorType.TOKEN_BLACKLISTED.getDesc(), ErrorType.TOKEN_BLACKLISTED);
                }
                Claims claims = jwtUtil.validateToken(jwt);
                String userId = claims.getSubject();
                String email = claims.get("email", String.class);
                UserRole userRole = UserRole.of(claims.get("userRole", String.class));
                UserDetails userDetails = new CustomUserDetail(userId,email,userRole);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, jwt, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            catch (ApiException e) {
                handleAuthException(response, e);
                return ;
            } catch (Exception e) {
                // 예상치 못한 에러 방어
                handleAuthException(response, new ApiException(ErrorType.UNAUTHORIZED.getDesc(), ErrorType.UNAUTHORIZED));
                return ;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleAuthException(HttpServletResponse response, ApiException e) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(e.getErrorType().getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        BaseExceptionResponse errorResponse = new BaseExceptionResponse(
                e.getErrorType().getStatus().value(),
                e.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
