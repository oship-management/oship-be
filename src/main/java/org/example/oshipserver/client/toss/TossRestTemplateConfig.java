package org.example.oshipserver.client.toss;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class TossRestTemplateConfig {

    @Bean
    public RestTemplate tossRestTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();

        // json 메시지 컨버터 설정
        restTemplate.setMessageConverters(List.of(new MappingJackson2HttpMessageConverter()));

        // Toss API 에러 핸들러 설정
        restTemplate.setErrorHandler(new TossApiResponseErrorHandler(objectMapper));

        return restTemplate;
    }
}
