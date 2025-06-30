package org.example.oshipserver.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TossApiResponseErrorHandlerTest {

    private TossApiResponseErrorHandler errorHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        errorHandler = new TossApiResponseErrorHandler(objectMapper);
    }

    @Test
    @DisplayName("4xx 응답이면 ApiException 발생 및 retry 대상이 아니다")
    void handleError_4xx_shouldThrowApiException_NoRetry() throws IOException {
        // given : Toss api가 400에러 응답 반환
        String body = """
            {
              "code": "ALREADY_PROCESSED_PAYMENT",
              "message": "이미 처리된 결제입니다."
            }
            """;
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(response.getBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        // when & then: handleError()가 ApiException을 던지며, 메시지에 "4xx", "재시도 불필요"가 포함되는지 확인
        assertThatThrownBy(() -> errorHandler.handleError(response))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("4xx")
            .hasMessageContaining("재시도 불필요");
    }

    @Test
    @DisplayName("5xx 응답이면 ApiException 발생 및 retry 대상이다")
    void handleError_5xx_shouldThrowApiException_Retryable() throws IOException {
        // given : Toss api가 500에러 응답 반환
        String body = """
            {
              "code": "INTERNAL_ERROR",
              "message": "서버 오류입니다."
            }
            """;
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(response.getBody()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        // when & then : handleError()가 ApiException을 던지며, 메시지에 "Toss API Error"가 포함되는지 확인
        assertThatThrownBy(() -> errorHandler.handleError(response))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Toss API Error");
    }

    @Test
    @DisplayName("hasError(): 4xx 응답이면 true 반환")
    void hasError_shouldReturnTrue_when4xx() throws IOException {
         // given : Toss api가 400에러 응답을 반환
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));

        // when : hasError() 호출
        boolean result = errorHandler.hasError(response);

        // then : 에러로 판단하여 true 반환
        assertTrue(result);
    }

    @Test
    @DisplayName("hasError(): 5xx 응답이면 true 반환")
    void hasError_shouldReturnTrue_when5xx() throws IOException {
        // given : Toss api가 500에러 응답을 반환
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));

        // when : hasError() 호출
        boolean result = errorHandler.hasError(response);

        // then : 에러로 판단하여 true 반환
        assertTrue(result);
    }

    @Test
    @DisplayName("hasError(): 2xx 응답이면 false 반환")
    void hasError_shouldReturnFalse_when2xx() throws IOException {
        // given : Toss api가 정상적인 200 응답을 반환
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(200));

        // when : hasError() 호출
        boolean result = errorHandler.hasError(response);

        // then : 정상응답으로 판단하여 false 반환
        assertFalse(result);
    }

    @Test
    @DisplayName("응답 바디가 JSON이 아닌 경우에도 ApiException 발생")
    void handleError_shouldHandleMalformedJsonGracefully() throws IOException {
        // given: Toss API에서 비정상적인 응답 포맷을 반환한 상황
        String malformedBody = "Not a JSON";
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(response.getBody()).thenReturn(new ByteArrayInputStream(malformedBody.getBytes(StandardCharsets.UTF_8)));

        // when & then: errorHandler가 예외를 던지는지 확인
        assertThatThrownBy(() -> errorHandler.handleError(response))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("Toss API Error");
    }


}
