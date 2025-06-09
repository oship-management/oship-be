package org.example.oshipserver.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.auth.vo.AccessTokenVo;
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
    private final RefreshTokenRepository refreshTokenRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String jwt = jwtUtil.extractTokenFromCookies(request, "access_token");
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
            catch (io.jsonwebtoken.ExpiredJwtException e){//토큰 유효기간 만료
                //토큰재발급 2차 구현
                //리프레시토큰 찾아오기
                //refresh토큰 찾아오려면 access_token에서 userId값 찾아야함
                Claims claims = e.getClaims();
                String userId = claims.getSubject();
                Long userIdLong = Long.valueOf(userId);
                String email = claims.get("email", String.class);
                String roleStr = claims.get("userRole", String.class);
                UserRole userRole = UserRole.of(roleStr);
                String refreshToken = refreshTokenRepository.getRefreshToken(userIdLong);
                //리프레시토큰이유효한지확인하기
                if(!jwtUtil.isRefreshTokenValid(refreshToken)){
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json; charset=UTF-8");
                    BaseExceptionResponse errorResponse = new BaseExceptionResponse(401, "유효하지 않은 토큰입니다");
                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    return ;
                }
                //유효하면 헤더에 어세스토큰 갈아넣어주기
                AccessTokenVo accessTokenVo = jwtUtil.createToken(userIdLong,email,userRole);
                //쿠키에 토큰 넣기
                JwtUtil.setCookieToken(response, accessTokenVo);
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
