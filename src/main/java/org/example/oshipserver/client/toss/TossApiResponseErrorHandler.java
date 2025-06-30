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
        // 4xx or 5xx는 모두 error로 간주
        return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        HttpStatusCode statusCode = response.getStatusCode();

        log.error("[Toss ErrorHandler] statusCode: {}, body: {}", statusCode, responseBody);

        String errorCode = null;
        try {
            Map<String, String> parsed = objectMapper.readValue(responseBody, Map.class);
            errorCode = parsed.get("code");
        } catch (Exception e) {
            log.warn("Toss 에러 바디 파싱 실패: {}", e.getMessage());
        }

        // 4xx 에러는 retry 없이 바로 ApiException
        if (statusCode.is4xxClientError()) {
            throw new ApiException("[4xx] " + errorCode + ": 재시도 불필요", ErrorType.TOSS_PAYMENT_FAILED);
        }

        // 5xx 등은 retry 대상
        throw new ApiException("[Toss API Error] " + statusCode, ErrorType.TOSS_PAYMENT_FAILED);
    }
}