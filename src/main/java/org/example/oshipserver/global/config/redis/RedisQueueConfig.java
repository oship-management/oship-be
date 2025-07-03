package org.example.oshipserver.global.config.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 이메일 큐 전용 RedisTemplate 설정
 * Key: String, Value: String (JSON 문자열 저장)
 */
@Configuration
public class RedisQueueConfig {

    @Bean(name = "stringQueueRedisTemplate")
    public RedisTemplate<String, String> stringQueueRedisTemplate(
        @Qualifier("redisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
