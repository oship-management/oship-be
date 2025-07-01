package org.example.oshipserver.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
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

@WebMvcTest(controllers = PaymentController.class
    , excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtFilter.class}) // Jwt 필터를 명시적으로 제외
}
)
@Import(PaymentControllerTest.MockedLogInterceptorConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

//    @Autowired
//    @MockBean
//    private JwtUtil jwtUtil;

//    private final JwtUtil jwtUtil = new JwtUtil("your-secret-key", 3600000L);

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

//        UserRole role = UserRole.SELLER;
//        String accessToken = jwtUtil.createToken(123L,"test@test.com", role);

        // when & then
        mockMvc.perform(post("/api/v1/payments/one-time")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer" + accessToken)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("단건 결제가 승인되었습니다."));
    }
}