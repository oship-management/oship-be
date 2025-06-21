package org.example.oshipserver.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.payment.entity.PaymentFailLog;
import org.example.oshipserver.domain.payment.repository.PaymentFailLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFailLogService {
    // toss api 호출이 최종 실패했을 때, 실패 내역을 PaymentFailLog에 저장

    private final PaymentFailLogRepository paymentFailLogRepository;

    public void saveFailLog(String url, Map<String, Object> body, String idempotencyKey) {
        try {
            PaymentFailLog failLog = PaymentFailLog.builder()
                .url(url)
                .requestBody(body.toString())  // JSON 직렬화 대신 단순 toString, 필요시 ObjectMapper 적용 가능
                .idempotencyKey(idempotencyKey)
                .failedAt(LocalDateTime.now())
                .build();

            paymentFailLogRepository.save(failLog);
            log.info("결제 실패 로그 저장 완료 - 키: {}", idempotencyKey);
        } catch (Exception e) {
            log.error("결제 실패 로그 저장 중 오류 발생", e);
        }
    }
}
