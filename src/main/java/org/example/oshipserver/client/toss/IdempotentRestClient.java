package org.example.oshipserver.client.toss;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class IdempotentRestClient { // 토스의 요청을 공통으로 처리

    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public <T, R> R postForIdempotent(
        String url,
        T body,
        Class<R> responseType,
        String idempotencyKey
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes())
        );
        headers.set("Idempotency-Key", idempotencyKey); // 헤더에 멱등성 키!

        HttpEntity<T> entity = new HttpEntity<>(body, headers);

        ResponseEntity<R> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            responseType
        );

        return response.getBody();
    }
}
