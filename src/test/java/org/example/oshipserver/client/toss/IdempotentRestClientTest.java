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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

@SpringBootTest
@TestPropertySource(properties = "toss.secret-key=test_sk")
class IdempotentRestClientRetryRecoverTest {

    @Autowired
    private IdempotentRestClient restClient;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private PaymentFailLogRepository paymentFailLogRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY = "recover-test-key";

    @Test
    void externalApiFails_retryOccurs_thenRecoverExecuted() throws Exception {
        // 외부 결제 api 호출이 실패할 때까지 retry가 3번 수행되고, 마지막에 @Recover 로직이 실행되는지
        // given : toss 결제승인api에 보낼 request body 준비
        Map<String, Object> requestBody = Map.of(
            "paymentKey", "abc123",
            "orderId", "ORD123",
            "amount", 10000
        );

        // Toss API 호출시, 항상 500에러 (retry 대상)
        when(restTemplate.exchange(
            any(), eq(HttpMethod.POST), any(), eq(TossPaymentConfirmResponse.class))
        ).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // recover 실행 시 필요한 설정
        Payment dummyPayment = Payment.builder()
            .paymentNo("1")
            .status(PaymentStatus.WAIT)
            .idempotencyKey(IDEMPOTENCY_KEY)
            .build();

        Order dummyOrder = Order.builder()
            .currentStatus(OrderStatus.PENDING)
            .build();
        ReflectionTestUtils.setField(dummyOrder, "id", 10L);

        PaymentOrder paymentOrder = PaymentOrder.builder()
            .order(dummyOrder)
            .build();

        dummyPayment.addPaymentOrder(paymentOrder);

        when(paymentRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
            .thenReturn(Optional.of(dummyPayment));

        // mock를 실제 직렬화 수행
        String json = objectMapper.writeValueAsString(requestBody);

        // when : toss api 호출시, 항상 ApiException 발생
        assertThrows(ApiException.class, () -> {
            restClient.postForIdempotent(URL, requestBody, TossPaymentConfirmResponse.class, IDEMPOTENCY_KEY);
        });

        // then : RestTemplate이 총 4번 호출되었는지 검증 (최초 1회 + 재시도 3회)
        verify(restTemplate, times(4)).exchange(eq(URL), eq(HttpMethod.POST), any(HttpEntity.class), eq(TossPaymentConfirmResponse.class));

        // then : recover 내부 로직이 제대로 수행되었는지 검증
        verify(paymentFailLogRepository).save(any(PaymentFailLog.class));
        verify(paymentRepository).save(dummyPayment);
        verify(orderRepository).save(dummyOrder);
    }
}
