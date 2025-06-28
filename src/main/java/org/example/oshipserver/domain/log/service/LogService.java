package org.example.oshipserver.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
    private final StringRedisTemplate redisTemplate;

    public void sendLogToRedis(String logJson) {
        log.info(logJson);
    }
}
