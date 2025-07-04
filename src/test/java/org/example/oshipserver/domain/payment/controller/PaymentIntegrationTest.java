package org.example.oshipserver.domain.payment.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.client.toss.IdempotentRestClient;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.response.TokenResponse;
import org.example.oshipserver.domain.notification.dto.request.NotificationRequest;
import org.example.oshipserver.domain.notification.service.EmailNotificationService;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentFullCancelRequest;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.repository.PaymentOrderRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.service.PaymentNotificationService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Testcontainers
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
class PaymentIntegrationTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean IdempotentRestClient idempotentRestClient;
    @SpyBean EmailNotificationService emailNotificationService;
    @SpyBean PaymentNotificationService paymentNotificationService;

    @Autowired OrderRepository orderRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired PaymentOrderRepository paymentOrderRepository;

    static String accessToken;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // 1. SELLER 회원가입
        String sellerSignupJson = """
        {
            "email": "gonaeunn@gmail.com",
            "password": "seller123!",
            "userRole": "SELLER",
            "firstName": "홍",
            "lastName": "길동",
            "phoneNo": "010-1234-5678",
            "companyName": "판매왕 주식회사",
            "companyRegisterNo": "111-22-14345",
            "companyTelNo": "02-123-4567"
        }
        """;

        mockMvc.perform(post("/api/v1/auth/sellers/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sellerSignupJson))
            .andExpect(status().isCreated());

        // 2. 로그인 및 토큰 추출
        LoginRequest loginRequest = new LoginRequest("gonaeunn@gmail.com", "seller123!");
        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        String content = result.getResponse().getContentAsString();

        JavaType responseType = objectMapper.getTypeFactory()
            .constructParametricType(BaseResponse.class, TokenResponse.class);

        BaseResponse<TokenResponse> baseResponse = objectMapper.readValue(content, responseType);
        accessToken = baseResponse.getData().accessToken();
    }

    // 전체 흐름을 테스트하지만 외부 api 호출만 모킹
    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("다건 결제 승인 성공 - PaymentOrder 저장, Order 상태 변경, 알림 전송까지")
    void confirmMultiPayment_success() throws Exception {
        // 1. 주문 저장
        Order order1 = orderRepository.save(Order.builder()
            .sellerId(1l)
            .currentStatus(OrderStatus.PENDING)
            .build());
        Order order2 = orderRepository.save(Order.builder()
            .sellerId(1l)
            .currentStatus(OrderStatus.PENDING)
            .build());

        // 2. Toss 응답 모킹
        TossPaymentConfirmResponse tossResponse = new TossPaymentConfirmResponse();
        tossResponse.setOrderId("multi-order-id-123");
        tossResponse.setPaymentKey("multi-payment-key-123");
        tossResponse.setStatus("DONE");
        tossResponse.setTotalAmount(10000);
        tossResponse.setCurrency("KRW");
        tossResponse.setMethod("간편결제");
        tossResponse.setApprovedAt("2025-07-04T11:00:00+09:00");

        TossPaymentConfirmResponse.EasyPay easyPay = new TossPaymentConfirmResponse.EasyPay();
        easyPay.setProvider("토스페이");
        tossResponse.setEasyPay(easyPay);

        TossPaymentConfirmResponse.Card card = new TossPaymentConfirmResponse.Card();
        card.setNumber("953*");
        tossResponse.setCard(card);

        TossPaymentConfirmResponse.Receipt receipt = new TossPaymentConfirmResponse.Receipt();
        receipt.setUrl("https://example.com/receipt");
        tossResponse.setReceipt(receipt);

        when(idempotentRestClient.postForIdempotent(
            anyString(), any(Map.class), any(Class.class), anyString()
        )).thenReturn(tossResponse);

        // 3. 요청 DTO
        MultiPaymentConfirmRequest request = new MultiPaymentConfirmRequest(
            "multi-payment-key-123",
            "multi-order-id-123",
            List.of(
                new MultiPaymentConfirmRequest.MultiOrderRequest(order1.getId(), 5000),
                new MultiPaymentConfirmRequest.MultiOrderRequest(order2.getId(), 5000)
            ),
            "KRW"
        );

        // 4. API 호출
        mockMvc.perform(post("/api/v1/payments/multi")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // 5. 검증
        Payment savedPayment = paymentRepository.findByPaymentKey("multi-payment-key-123")
            .orElseThrow();
        assertThat(savedPayment.getAmount()).isEqualTo(10000);

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment_Id(savedPayment.getId());
        assertThat(paymentOrders).hasSize(2);

        Order updated1 = orderRepository.findById(order1.getId()).orElseThrow();
        Order updated2 = orderRepository.findById(order2.getId()).orElseThrow();
        assertThat(updated1.getCurrentStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(updated2.getCurrentStatus()).isEqualTo(OrderStatus.PAID);

        verify(paymentNotificationService, times(1)).sendPaymentCompletedV2(any());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("전체 결제 취소 성공 - Payment 상태, Order 상태, 알림 및 이력까지 검증")
    void cancelFullPayment_success() throws Exception {
        // 1. 주문 2건 저장 (PAID 상태)wjz
        Order order1 = orderRepository.save(Order.builder()
            .sellerId(1L)
            .currentStatus(OrderStatus.PAID)
            .build());
        Order order2 = orderRepository.save(Order.builder()
            .sellerId(1L)
            .currentStatus(OrderStatus.PAID)
            .build());

        // 2. 결제 저장 (모든 NOT NULL 필드 포함)
        Payment payment = paymentRepository.save(Payment.builder()
            .paymentNo("PAY-TEST-0001")
            .paymentKey("cancel-payment-key-123")
            .idempotencyKey("cancel-idempotent-key-123")
            .amount(10000)
            .status(PaymentStatus.COMPLETE)
            .currency("KRW")
            .sellerId(1L)
            .method(PaymentMethod.EASY_PAY_CARD)
            .paidAt(LocalDateTime.now())
            .build());

        payment.setCardLast4Digits("953*");
        payment.setReceiptUrl("https://example.com/receipt");

        payment = paymentRepository.save(payment);

        // 3. 연결된 PaymentOrder 저장
        paymentOrderRepository.save(PaymentOrder.of(payment, order1, 5000));
        paymentOrderRepository.save(PaymentOrder.of(payment, order2, 5000));

        // 4. Toss 응답 모킹 (취소 응답)
        when(idempotentRestClient.postForIdempotent(
            eq("/v1/payments/cancel"),
            any(Map.class),
            eq(Void.class),
            eq("cancel-payment-key-123")
        )).thenReturn(null);

        // 5. 요청 API 실행
        PaymentFullCancelRequest cancelRequest = new PaymentFullCancelRequest("테스트 전체 취소");

        mockMvc.perform(post("/api/v1/payments/cancel-payment-key-123/cancel/full")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
            .andExpect(status().isOk());

        // 6. 검증

        // Payment 상태
        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.CANCEL);

        // PaymentOrder 상태 + 취소 시간
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment_Id(payment.getId());
        assertThat(paymentOrders).allSatisfy(po -> {
            assertThat(po.getPaymentStatus()).isEqualTo(PaymentStatus.CANCEL);
            assertThat(po.getCanceledAt()).isNotNull();
        });

        // Order 상태
        Order updated1 = orderRepository.findById(order1.getId()).orElseThrow();
        Order updated2 = orderRepository.findById(order2.getId()).orElseThrow();
        assertThat(updated1.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);
        assertThat(updated2.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);

        // 이메일 알림 호출 여부
        verify(emailNotificationService, times(1)).send(any(NotificationRequest.class));
    }

}