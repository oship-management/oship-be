package org.example.oshipserver.client.toss;

import java.util.Map;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentFailLog;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.repository.PaymentFailLogRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import static org.junit.jupiter.api.Assertions.*;

class IdempotentRestClientRetryRecoverTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentFailLogRepository paymentFailLogRepository = mock(PaymentFailLogRepository.class);
    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);

    private final IdempotentRestClient restClient = new IdempotentRestClient(
        restTemplate,
        objectMapper,
        paymentFailLogRepository,
        paymentRepository,
        orderRepository
    );

    private static final String URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY = "recover-test-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(restClient, "tossSecretKey", "test_sk");
    }

    @Test
    void retryTemplate_retries_4_times_on_5xx_error() {

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
            .maxAttempts(4)  // 1회 시도 + 3회 재시도
            .fixedBackoff(10)  // 10ms backoff
            .build();

        // when : toss api 호출시, 항상 ApiException 발생
        assertThrows(ApiException.class, () -> {
            template.execute(context -> {
                return restClient.postForIdempotent(URL, requestBody, TossPaymentConfirmResponse.class, IDEMPOTENCY_KEY);
            });
        });

        // then : RestTemplate이 총 4번 호출되었는지 검증 (최초 1회 + 재시도 3회)
        verify(restTemplate, times(4)).exchange(
            eq(URL),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(TossPaymentConfirmResponse.class)
        );
    }
}
