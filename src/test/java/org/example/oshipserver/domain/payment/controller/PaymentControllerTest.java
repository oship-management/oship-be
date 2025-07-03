package org.example.oshipserver.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.order.dto.response.OrderPaymentResponse;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentFullCancelRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentPartialCancelRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentCancelHistoryResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.UserPaymentLookupResponse;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.service.PaymentService;
import org.example.oshipserver.domain.log.service.LogService;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.common.component.LogInterceptor;
import org.example.oshipserver.global.common.utils.JwtUtil;
import org.example.oshipserver.global.config.JwtFilter;
import org.example.oshipserver.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@WebMvcTest(controllers = PaymentController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtFilter.class}) // Jwt 필터를 명시적으로 제외
})
@Import(PaymentControllerTest.MockedLogInterceptorConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TestConfiguration
    static class MockedLogInterceptorConfig {
        @Bean
        public LogInterceptor logInterceptor() {
            LogService mockLogService = Mockito.mock(LogService.class);
            ObjectMapper objectMapper = new ObjectMapper();
            return new LogInterceptor(objectMapper, mockLogService);
        }
    }

    @Test
    @DisplayName("단건결제 승인 요청시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "SELLER")  // 인증된 사용자인 척
    void 단건결제승인_요청시_성공응답을_반환한다() throws Exception {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "paymentKey123",
            123L,
            "orderId123",
            10000
        );
        PaymentConfirmResponse mockResponse = new PaymentConfirmResponse(
            "paymentId123",
            "orderId123",
            PaymentStatus.COMPLETE,
            "2025-07-01T10:00:00",
            10000,
            "KRW",
            PaymentMethod.EASY_PAY_CARD,
            "1234",
            "https://receipt.url"
        );

        given(paymentService.confirmPayment(any())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/payments/one-time")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("단건 결제가 승인되었습니다."));
    }

    @Test
    @DisplayName("다건결제 승인 요청시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "SELLER")
    void 다건결제승인_요청시_성공응답을_반환한다() throws Exception {
        // given
        MultiPaymentConfirmRequest request = new MultiPaymentConfirmRequest(
            "tgen_20250612100748JLbH3",
            "MC43NDkyNTgxNjQyODM0",
            List.of(
                new MultiPaymentConfirmRequest.MultiOrderRequest(201L, 30000),
                new MultiPaymentConfirmRequest.MultiOrderRequest(202L, 20000)
            ),
            "KRW"
        );

        MultiPaymentConfirmResponse response = new MultiPaymentConfirmResponse(
            List.of("201", "202"),
            "tgen_20250612100748JLbH3",
            PaymentStatus.COMPLETE,
            "2025-06-12T10:08:43+09:00",
            50000,
            "KRW",
            "953*",
            "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tgen_20250612100748JLbH3&ref=PX"
        );

        given(paymentService.confirmMultiPayment(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/payments/multi")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("다건 결제가 승인되었습니다."))
            .andExpect(jsonPath("$.data.paymentKey").value("tgen_20250612100748JLbH3"))
            .andExpect(jsonPath("$.data.orderIds[0]").value("201"))
            .andExpect(jsonPath("$.data.orderIds[1]").value("202"))
            .andExpect(jsonPath("$.data.totalAmount").value(50000))
            .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETE"))
            .andExpect(jsonPath("$.data.cardLast4Digits").value("953*"))
            .andExpect(jsonPath("$.data.receiptUrl").value("https://dashboard.tosspayments.com/receipt/redirection?transactionId=tgen_20250612100748JLbH3&ref=PX"));
    }

    @Test
    @DisplayName("전체 결제 취소 요청시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "SELLER")
    void 전체결제취소_요청시_성공응답을_반환한다() throws Exception {
        // given
        String paymentKey = "tgen_20250612100748JLbH3";
        String cancelReason = "고객 요청";

        // 요청 객체 생성
        PaymentFullCancelRequest request = new PaymentFullCancelRequest(cancelReason);

        // cancelFullPayment은 void 메소드이므로 mocking만 설정해도 충분
        Mockito.doNothing().when(paymentService).cancelFullPayment(paymentKey, cancelReason);

        // when & then
        mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel/full", paymentKey)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("전체 결제가 성공적으로 취소되었습니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("결제 부분 취소 요청시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "SELLER")
    void 결제부분취소_요청시_성공응답을_반환한다() throws Exception {
        // given
        String paymentKey = "tgen_20250612100748JLbH3";
        Long orderId = 7L;
        String cancelReason = "부분 취소 테스트";

        // 요청 DTO 생성
        PaymentPartialCancelRequest request = new PaymentPartialCancelRequest(orderId, cancelReason);

        // 서비스 호출 mocking (void)
        Mockito.doNothing().when(paymentService).cancelPartialPayment(paymentKey, orderId, cancelReason);

        // when & then
        mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel/partial", paymentKey)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("부분 결제가 성공적으로 취소되었습니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }


    @Test
    @DisplayName("결제 취소 이력 조회 요청시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "SELLER")
    void 결제취소이력_조회시_성공응답을_반환한다() throws Exception {
        // given
        String paymentKey = "tgen_20250612100748JLbH3";

        List<PaymentCancelHistoryResponse> mockHistories = List.of(
            new PaymentCancelHistoryResponse(
                7L,
                35000,
                "테스트용 부분 취소",
                LocalDateTime.parse("2025-06-19T12:17:07.482209"),
                PaymentStatus.PARTIAL_CANCEL
            ),
            new PaymentCancelHistoryResponse(
                null,
                15000,
                "테스트용 전체 취소",
                LocalDateTime.parse("2025-06-19T12:19:45.269524"),
                PaymentStatus.CANCEL
            )
        );

        given(paymentService.getCancelHistory(paymentKey)).willReturn(mockHistories);

        // when & then
        mockMvc.perform(get("/api/v1/payments/" + paymentKey + "/cancel-history")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("결제 취소 이력 조회 완료"))
            // 첫 번째 이력: 부분 취소
            .andExpect(jsonPath("$.data[0].orderId").value(7))
            .andExpect(jsonPath("$.data[0].cancelAmount").value(35000))
            .andExpect(jsonPath("$.data[0].cancelReason").value("테스트용 부분 취소"))
            .andExpect(jsonPath("$.data[0].canceledAt").value("2025-06-19T12:17:07.482209"))
            .andExpect(jsonPath("$.data[0].paymentStatus").value("PARTIAL_CANCEL"))
            // 두 번째 이력: 전체 취소
            .andExpect(jsonPath("$.data[1].orderId").doesNotExist())
            .andExpect(jsonPath("$.data[1].cancelAmount").value(15000))
            .andExpect(jsonPath("$.data[1].cancelReason").value("테스트용 전체 취소"))
            .andExpect(jsonPath("$.data[1].canceledAt").value("2025-06-19T12:19:45.269524"))
            .andExpect(jsonPath("$.data[1].paymentStatus").value("CANCEL"));
    }

    @Test
    @DisplayName("판매자 ID 기준 결제 내역 조회시 성공 응답 반환")
    @WithMockUser(username = "123", roles = "ADMIN")
    void 판매자기준_결제내역조회시_성공응답을_반환한다() throws Exception {
        // given
        Long sellerId = 123L;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        List<PaymentLookupResponse> mockResponse = List.of(
            new PaymentLookupResponse(
                1L,
                "MC123456789",
                "tgen_001",
                PaymentStatus.COMPLETE,
                "2025-06-19T12:10:00",
                50000,
                "KRW",
                "1234",
                "https://receipt.url",
                List.of() // order 리스트 (mock이므로 비워도 됨)
            )
        );

        given(paymentService.getPaymentsBySellerId(sellerId, startDate, endDate))
            .willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/v1/payments/seller/{sellerId}", sellerId)
                .param("startDate", "2025-06-01")
                .param("endDate", "2025-06-30")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("판매자 결제 내역 조회 완료"))
            .andExpect(jsonPath("$.data[0].paymentId").value(1))
            .andExpect(jsonPath("$.data[0].tossOrderId").value("MC123456789"))
            .andExpect(jsonPath("$.data[0].paymentKey").value("tgen_001"))
            .andExpect(jsonPath("$.data[0].paymentStatus").value("COMPLETE"))
            .andExpect(jsonPath("$.data[0].paidAt").value("2025-06-19T12:10:00"))
            .andExpect(jsonPath("$.data[0].amount").value(50000))
            .andExpect(jsonPath("$.data[0].currency").value("KRW"))
            .andExpect(jsonPath("$.data[0].cardLast4Digits").value("1234"))
            .andExpect(jsonPath("$.data[0].receiptUrl").value("https://receipt.url"));

    }

    @Test
    @DisplayName("나의 결제 내역 조회 요청시 성공 응답 반환")
    void 나의결제내역_조회시_성공응답을_반환한다() throws Exception {
        // given
        Long userId = 123L;
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 30);

        // 모의 응답 설정
        List<OrderPaymentResponse> orderResponses = List.of(
            new OrderPaymentResponse(5014L, "ORD-20250619-001", 2, new BigDecimal("1.8"),
                "CANCELLED", "김예은", "HongGil", 30000, "OSH250619US5E2BC69")
        );

        List<UserPaymentLookupResponse> mockResponse = List.of(
            new UserPaymentLookupResponse(
                14L,
                PaymentStatus.PARTIAL_CANCEL.name(),
                "2025-06-19T09:43:28",
                50000,
                "KRW",
                "953*",
                "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tgen_20250619094246mBOQ0&ref=PX",
                orderResponses
            )
        );

        given(paymentService.getPaymentsByUser(userId, startDate, endDate)).willReturn(mockResponse);

        // 인증 사용자 설정
        CustomUserDetail userDetail = new CustomUserDetail("123", "test@email.com", UserRole.SELLER);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userDetail,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
        );

        // when & then
        mockMvc.perform(get("/api/v1/payments/mypayments")
                .param("startDate", "2025-06-01")
                .param("endDate", "2025-06-30")
                .with(authentication(auth))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("나의 결제 내역 조회 완료"));
    }
}