package org.example.oshipserver.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.payment.dto.request.FailedTossRequestDto;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentFailLog;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.repository.PaymentFailLogRepository;
import org.example.oshipserver.domain.payment.repository.PaymentOrderRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
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
    private final ObjectMapper objectMapper;
    private final PaymentFailLogRepository paymentFailLogRepository;
    private final PaymentRepository paymentRepository;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    // ✅ 테스트용 강제 실패 플래그 추가
    private boolean forceFail = false;

    public void setForceFail(boolean forceFail) {
        this.forceFail = forceFail;
    }

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
        maxAttempts = 2,                 // 총 2번 (최초 1회 시도 + 1회 재시도)
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public <T, R> R postForIdempotent(
        String url,
        T body,
        Class<R> responseType,
        String idempotencyKey
    ) {
        long start = System.currentTimeMillis();

        // ✅  retry 테스트용 : 강제 실패 URL 덮어쓰기
//        url = "https://api.tosspayments.com/this-path-does-not-exist";

        // 재시도 로그  남기기
        log.warn("[RETRYABLE] Toss API 호출 시도 - idempotencyKey={}, url={}, retryCount={}",
            idempotencyKey,
            url,
            RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount()
                : 0
        );

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

            // toss응답이 성공적이여도 body가 null이면 재시도하도록
            if (response.getBody() == null || response.getBody().getClass().getDeclaredMethod("getPaymentKey") != null &&
                response.getBody().getClass().getMethod("getPaymentKey").invoke(response.getBody()) == null) {
                log.error("[RETRYABLE] Toss 응답은 200이지만 내용이 비정상(null)입니다 (재시도)");
                throw new ApiException("Toss 응답이 비정상입니다", ErrorType.TOSS_PAYMENT_FAILED);
            }

            // Toss 응답 객체 → JSON 문자열로 변환
            try {
                log.info("[Toss 원문 응답 바디] {}", objectMapper.writeValueAsString(response.getBody()));
            } catch (Exception ex) {
                log.warn("[Toss 응답 바디 JSON 직렬화 실패] {}", ex.getMessage(), ex);
            }

            // 성공 응답 로그
            int retryCount = RetrySynchronizationManager.getContext() != null ?
                RetrySynchronizationManager.getContext().getRetryCount() : 0;

            long duration = System.currentTimeMillis() - start;

            if (retryCount == 0) {
                log.info("Toss API 첫 시도에 성공 - idempotencyKey={}, duration={}ms", idempotencyKey, duration);
            } else {
                log.info("[RETRYABLE] Toss API 재시도 후 성공 - idempotencyKey={}, retryCount={}, duration={}ms",
                    idempotencyKey, retryCount, duration);
            }
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Toss API에서 에러 응답이 온 경우
            log.error("[Toss 호출 실패] 상태코드: {}", e.getStatusCode());
            log.error("[Toss 에러 바디] {}", e.getResponseBodyAsString());
            // @retryable이 예외를 인식할 수 있도록
            throw new ApiException("Toss 호출 실패: " + e.getResponseBodyAsString(), ErrorType.TOSS_PAYMENT_FAILED);
        } catch (Exception e) {
            // 네트워크 오류 등 기타 예외
            log.error("[Toss 호출 예외 발생] {}", e.getMessage(), e);
            throw new ApiException("Toss 호출 중 알 수 없는 오류 발생", e);
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;
            log.warn("[RETRYABLE] Toss API 최종 요청 소요 시간: {} ms (idempotencyKey={})", duration, idempotencyKey);
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
        long recoverStart = System.currentTimeMillis();  // 장애 대응 시간 측정 시작
        log.error("Toss 결제 요청 최종 재시도 실패. 실패 로그를 DB에 기록합니다.");

        try {
            String bodyJson = objectMapper.writeValueAsString(body); // json 직렬화

            PaymentFailLog failLog = PaymentFailLog.builder()
                .url(url)
                .requestBody(bodyJson)
                .idempotencyKey(idempotencyKey)
                .errorMessage(e.getMessage())
                .build();

            paymentFailLogRepository.save(failLog); // db 저장
            long recoverEnd = System.currentTimeMillis();  // 로그 저장 시점
            long totalDuration = recoverEnd - recoverStart;

            log.warn("결제 실패 로그 저장 완료: idempotencyKey={}", idempotencyKey);
            log.warn("[RECOVER] 장애 대응 총 소요 시간: {} ms (idempotencyKey={})", totalDuration, idempotencyKey);

            // 결재 실패시, payment 엔티티는 생성되지 않기 때문에, payment/order 상태 변경 없이 fail 로그만 남김
            // payment가 생성되었을 가능성을 대비하여 존재 여부만 체크
            Optional<Payment> optionalPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (optionalPayment.isPresent()) {
                log.warn("결제 실패 상황에서 이미 생성된 payment 존재: paymentNo={}", optionalPayment.get().getPaymentNo());
            } else {
                log.warn("결제 실패 상황에서 payment 미생성 상태 (idempotencyKey={})", idempotencyKey);
            }

        } catch (Exception ex) {
            log.error("결제 실패 로그 저장 또는 상태 확인 중 오류 발생: {}", ex.getMessage(), ex);
        }

        // 사용자에게 최종 실패 응답 반환
        throw new ApiException(ErrorType.TOSS_PAYMENT_FAILED);
    }
}
