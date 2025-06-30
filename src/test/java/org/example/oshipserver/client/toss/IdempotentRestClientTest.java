package org.example.oshipserver.client.toss;

import java.util.Map;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.repository.PaymentFailLogRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.web.client.HttpClientErrorException;
import java.nio.charset.StandardCharsets;

class IdempotentRestClientRetryRecoverTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentFailLogRepository paymentFailLogRepository = mock(PaymentFailLogRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);

    private final IdempotentRestClient restClient = new IdempotentRestClient(
        restTemplate,
        objectMapper,
        paymentFailLogRepository,
        paymentRepository
    );

    private static final String URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY = "recover-test-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(restClient, "tossSecretKey", "test_sk");
    }

    @DisplayName("정상동작: Toss API가 500 에러를 반환하면 @Retryable 작동")
    @Test
    void retryTemplate_retries_1_times_on_5xx_error() { // retry 설정 검증

        // given : toss 결제승인api에 보낼 request body 준비
        Map<String, Object> requestBody = Map.of(
            "paymentKey", "abc123",
            "orderId", "ORD123",
            "amount", 10000
        );

        // RestTemplate이 항상 500에러 반환하게 mock
        when(restTemplate.exchange(
            eq(URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(TossPaymentConfirmResponse.class))
        ).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // RetryTemplate 구성
        RetryTemplate template = RetryTemplate.builder()
            .maxAttempts(2)  // 1회 시도 + 1회 재시도
            .fixedBackoff(10)  // 테스트용 최소 대기시간
            .build();

        // when : toss api 호출시, 항상 ApiException 발생
        assertThrows(ApiException.class, () -> {
            template.execute(context -> {
                return restClient.postForIdempotent(URL, requestBody, TossPaymentConfirmResponse.class, IDEMPOTENCY_KEY);
            });
        });

        // then : RestTemplate이 총 2번 호출되었는지 검증 (최초 1회 + 재시도 1회)
        verify(restTemplate, times(2)).exchange(
            eq(URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(TossPaymentConfirmResponse.class)
        );
    }

    @DisplayName("문제인식: 고객 잘못의 400 에러에도 @Retryable이 작동함")
    @Test
    void 고객잘못_400에러인데도_retry가_발생하는_문제() {
        // 이전의 mock 기반 구조 유지하기 때문에 리팩토링 이후에도 여전히 해당 테스트 성공함 (문제 인식 기록용)
        // 리팩토링 이후에는 errorHandler가 적용된 RestTemplate을 연결해야 실제 작동을 확인할 수 있음 (통합테스트로 확인할것)

        // given : 요청 본문 구성 및 toss api의 400 오류 상황 구성
        Map<String, Object> requestBody = Map.of(
            "paymentKey", "abc123",
            "orderId", "ORD123",
            "amount", 10000
        );

        // Toss API에서 ALREADY_PROCESSED_PAYMENT 오류 발생하도록 mock
        String errorJson = """
        {
          "code": "ALREADY_PROCESSED_PAYMENT",
          "message": "이미 처리된 결제입니다."
        }
        """;

        // RestTemplate을 mock하여 400 응답을 항상 반환하게 설정
        when(restTemplate.exchange(
            eq(URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(TossPaymentConfirmResponse.class))
        ).thenThrow(new HttpClientErrorException(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            errorJson.getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8
        ));

        // RetryTemplate 구성: 재시도(총 2회)하도록 설정
        RetryTemplate template = RetryTemplate.builder()
            .maxAttempts(2)  // 기대대로라면 이 maxAttempts를 넘지 않아야 함
            .fixedBackoff(10)
            .build();

        // when : Toss 결제시 api 호출 시도 (HttpClientErrorException(400)이 발생하면 ApiException으로 변환되어 던져짐)
        assertThrows(ApiException.class, () -> {
            template.execute(context -> {
                return restClient.postForIdempotent(URL, requestBody, TossPaymentConfirmResponse.class, IDEMPOTENCY_KEY);
            });
        });

        // then : retry가 일어났는지 검증
        // 400오류일때 실제로는 재시도가 없어야하지만, 2번 호출(retry실행)되어 문제 상황 인식함
        verify(restTemplate, times(2)).exchange(
            eq(URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(TossPaymentConfirmResponse.class)
        );
    }
}