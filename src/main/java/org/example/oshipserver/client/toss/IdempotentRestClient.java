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
    private final OrderRepository orderRepository;

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
            log.warn("결제 실패 로그 저장 완료: idempotencyKey={}", idempotencyKey);

            // 상태 업데이트
            Optional<Payment> optionalPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (optionalPayment.isPresent()) {
                Payment payment = optionalPayment.get();
                try {
                    payment.updateStatus(PaymentStatus.FAIL);  // 결제 상태 변경
                    paymentRepository.save(payment);
                    log.info("결제 상태를 FAIL로 변경했습니다. paymentNo={}", payment.getPaymentNo());
                } catch (IllegalStateException ex) {
                    log.warn("결제 상태 FAIL로 변경 실패: 현재 상태={}, paymentNo={}, reason={}",
                        payment.getStatus(), payment.getPaymentNo(), ex.getMessage());
                }

                for (PaymentOrder po : payment.getPaymentOrders()) {
                    Order order = po.getOrder();
                    try {
                        order.markAs(OrderStatus.FAILED); // 주문 상태 변경
                        orderRepository.save(order);
                    } catch (IllegalStateException ex) {
                        log.warn("주문 상태를 FAILED로 전이할 수 없음: orderId={}, currentStatus={}, reason={}",
                            order.getId(), order.getCurrentStatus(), ex.getMessage());
                    }
                }

                log.warn("결제 및 주문 상태 FAIL / FAILED 로 변경 완료: paymentNo={}", payment.getPaymentNo());
            }
        } catch (Exception ex) {
            log.error("결제 실패 로그 저장 또는 상태 업데이트 중 오류 발생: {}", ex.getMessage(), ex);
        }

        // 사용자에게 최종 실패 응답 반환
        throw new ApiException(ErrorType.TOSS_PAYMENT_FAILED);
    }
}
