package org.example.oshipserver.client.toss;

import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class IdempotentRestClient { // 토스의 post 요청을 멱등성 방식으로 처리

    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    /**
     * 멱등성 post 요청 처리 메서드
     * @param url
     * @param body
     * @param responseType
     * @param idempotencyKey 멱등성 키
     * @return Toss 응답객체
     */
    public <T, R> R postForIdempotent(
        String url,
        T body,
        Class<R> responseType,
        String idempotencyKey
    ) {
        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Authorization 헤더 설정
        headers.set("Authorization", "Basic " +
            Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8))
        );

        // 멱등성 키 헤더 설정
        headers.set("Idempotency-Key", idempotencyKey); // 헤더에 멱등성 키!

        // 요청 엔티티 구성
        HttpEntity<T> entity = new HttpEntity<>(body, headers);

        // RestTemplate을 통해 post 요청
        ResponseEntity<R> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            responseType
        );

        return response.getBody();
    }
}
