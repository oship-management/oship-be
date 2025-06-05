package org.example.oshipserver.client.toss;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TossRestTemplateConfig {

    @Bean
    public RestTemplate tossRestTemplate() {
        return new RestTemplate();  // 기본 HTTP 클라이언트 생성 (토스 API 호출)
    }
}
