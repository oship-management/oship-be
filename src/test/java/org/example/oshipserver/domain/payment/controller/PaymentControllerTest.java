package org.example.oshipserver.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

}