package org.example.oshipserver.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
    private final RedisTemplate<String, String> redisTemplate;

    public void sendLogToRedis(String logJson) {
        log.info(logJson);
    }
}
