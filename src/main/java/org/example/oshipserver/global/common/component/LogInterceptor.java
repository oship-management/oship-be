package org.example.oshipserver.global.common.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.log.service.LogService;
import org.example.oshipserver.domain.log.vo.LogInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class LogInterceptor implements HandlerInterceptor {
    private final ThreadLocal<LogInfo> logInfoThreadLocal = new ThreadLocal<>();
    private final ObjectMapper objectMapper;
    private final LogService logService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = authentication.getPrincipal().equals("anonymousUser") ? null : Long.valueOf(authentication.getName());
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            uri += "?" + queryString;
        }
        LogInfo logInfo = LogInfo.builder()
                .date(date)
                .userId(userId)
                .method(request.getMethod())
                .uri(uri)
                .userAgent(request.getHeader("User-Agent"))
                .ip(request.getRemoteAddr())
                .duration(System.currentTimeMillis())
                .build();
        logInfoThreadLocal.set(logInfo);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            LogInfo logInfo = logInfoThreadLocal.get();
            long duration = System.currentTimeMillis() - logInfo.getDuration();
            logInfo.setDuration(duration);
            logInfo.setStatus(response.getStatus());
            String log = objectMapper.writeValueAsString(logInfo);
            logService.sendLogToRedis(log);
        }finally {
            logInfoThreadLocal.remove();
            HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        }
    }
}
