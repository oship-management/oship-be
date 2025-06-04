package org.example.oshipserver.client.toss;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TossRestTemplateConfig {

    @Bean
    public RestTemplate tossRestTemplate() {
        return new RestTemplate();
    }
}
