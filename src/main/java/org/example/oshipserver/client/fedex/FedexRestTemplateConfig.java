
package org.example.oshipserver.client.fedex;

import java.io.IOException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FedexRestTemplateConfig {

    // 타임아웃 상수 정의
    private static final int CONNECT_TIMEOUT_MS = 60000;           // 60초
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 60000; // 60초
    private static final int HTTP_STATUS_BAD_REQUEST = 400;
    private static final int HTTP_STATUS_UNAUTHORIZED = 401;

    @Bean
    @Primary
    public RestTemplate fedexRestTemplate() {

        // Apache HttpClient 사용 (gzip 자동 해제 지원)
        CloseableHttpClient httpClient = HttpClients.custom()
            .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS);

        RestTemplate restTemplate = new RestTemplate(factory);

        // 에러 핸들러 설정 - 400, 401은 예외를 던지지 않도록 함
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                // 400, 401은 에러로 처리하지 않음 (직접 처리하기 위해)
                HttpStatusCode statusCode = response.getStatusCode();
                return statusCode.value() != HTTP_STATUS_BAD_REQUEST
                    && statusCode.value() != HTTP_STATUS_UNAUTHORIZED
                    && statusCode.isError();
            }

        });

        return restTemplate;  // 기본 HTTP 클라이언트 생성 (FedEx API 호출)
    }
}