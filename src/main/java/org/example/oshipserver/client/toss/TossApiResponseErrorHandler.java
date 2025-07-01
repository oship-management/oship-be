package org.example.oshipserver.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TossApiResponseErrorHandler implements ResponseErrorHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        // Toss API 응답이 4xx 또는 5xx일 경우 에러로 판단
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // 위 hasError()가 true를 반환한 경우 실행됨
        // Toss API 응답 바디를 문자열로 읽어옴
        String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        HttpStatusCode statusCode = response.getStatusCode();

        log.error("[Toss ErrorHandler] statusCode: {}, body: {}", statusCode, responseBody);

        // Toss에서 제공하는 에러 코드 추출
        String errorCode = null;
        try {
            Map<String, String> parsed = objectMapper.readValue(responseBody, Map.class);
            errorCode = parsed.get("code");
        } catch (Exception e) {
            log.warn("Toss 에러 바디 파싱 실패: {}", e.getMessage());
        }

        // 예외 분기처리
        if (statusCode.is4xxClientError()) {
            // 4xx: 고객 귀책 에러(재시도하면 안 됨) → ApiException 발생시켜 retry 방지
            throw new ApiException("[4xx] " + errorCode + ": 재시도 불필요", ErrorType.TOSS_PAYMENT_FAILED);
        }

        // 5xx: Toss 서버 문제(재시도 대상) → ApiException 발생시켜 @Retryable 작동 유도
        throw new ApiException("[Toss API Error] " + statusCode, ErrorType.TOSS_PAYMENT_FAILED);
    }
}