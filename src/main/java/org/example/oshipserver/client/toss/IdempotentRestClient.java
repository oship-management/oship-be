package org.example.oshipserver.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.payment.dto.request.FailedTossRequestDto;
import org.example.oshipserver.global.common.component.RedisService;
import org.example.oshipserver.global.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotentRestClient { // 토스의 post 요청을 멱등성 방식으로 처리

    private final RestTemplate restTemplate;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    /**
     * 멱등성 post 요청 처리 메서드
     *
     * @param url
     * @param body
     * @param responseType
     * @param idempotencyKey 멱등성 키
     * @return Toss 응답객체
     */
    @Retryable(
        value = { ApiException.class },  // 재시도할 예외
        maxAttempts = 4,                 // 총 4번 (최초 1회 시도 + 3회 재시도)
        backoff = @Backoff(delay = 2000, multiplier = 2) // 점점 지연시간 증가
    )
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
            Base64.getEncoder()
                .encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8))
        );

        // 멱등성 키 헤더 설정
        headers.set("Idempotency-Key", idempotencyKey); // 헤더에 멱등성 키!

        // 요청 엔티티 구성
        HttpEntity<T> entity = new HttpEntity<>(body, headers);

        // RestTemplate을 통해 post 요청
        try {
            ResponseEntity<R> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                responseType
            );

            // 성공 응답 로그
            log.info("[Toss 성공 응답] {}", response.getBody());
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Toss API에서 에러 응답이 온 경우
            log.error("[Toss 호출 실패] 상태코드: {}", e.getStatusCode());
            log.error("[Toss 에러 바디] {}", e.getResponseBodyAsString());
            throw new ApiException("Toss 호출 실패: " + e.getResponseBodyAsString(), e);

        } catch (Exception e) {
            // 네트워크 오류 등 기타 예외
            log.error("[Toss 호출 예외 발생] {}", e.getMessage(), e);
            throw new ApiException("Toss 호출 중 알 수 없는 오류 발생", e);
        }
    }

    @Recover
    public <R> R recover(
        ApiException e, // 최종 예외
        String url, // toss api
        Map<String, Object> body,
        Class<R> responseType,
        String idempotencyKey
    ) {
        log.error("Toss 결제 요청 최종 재시도 실패. 실패한 요청을 Redis 큐에 적재합니다.");
        try {
            FailedTossRequestDto failedRequest = new FailedTossRequestDto(url, body, idempotencyKey);
            String json = objectMapper.writeValueAsString(failedRequest); // json 직렬화
            redisService.pushToList("failed:toss:payment", json);     // Redis 적재

            log.info("Redis 적재 완료: {}", json);
        } catch (Exception ex) {
            log.error("Redis 적재 실패: {}", ex.getMessage(), ex);
        }

        // 사용자에게 일시적 실패라는 응답 전달. 이후 10분간 재처리(retry)
        throw new ApiException("현재 일시적 장애로 인해 결제를 완료할 수 없습니다. 10분 후에 다시 확인해주세요.");
    }

}
