package org.example.oshipserver.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.payment.dto.request.FailedTossRequestDto;
import org.example.oshipserver.client.toss.TossPaymentClient;
import org.example.oshipserver.global.common.component.RedisService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

//@Slf4j
//@Service
//@RequiredArgsConstructor
public class FailedRequestRetryService {
    /**
     * Redis 큐에 쌓인 Toss의 실패 요청을 스케줄러를 사용하여 재처리하는 로직
     * 현재 단건/다건 결제에서는 사용하지 않지만, 추후 정기결제에서 사용하거나
     * 외부 api 실패 재처리를 위해 다시 사용할 수 있으므로 주석처리
     */

//    private final RedisService redisService;
//    private final ObjectMapper objectMapper;
//    private final TossPaymentClient tossPaymentClient;
//
//    private static final String REDIS_KEY = "failed:toss:requests";
//
//    @Scheduled(fixedDelay = 600_000) // 10분마다 메서드가 주기적으로 실행됨 (스케줄러)
//    public void retryFailedTossRequests() {
//        while (true) {
//            String json = redisService.popFromList(REDIS_KEY);  // redis에서 pop해서 실패 요청을 하나씩 꺼냄
//            if (json == null) break;  // 큐가 비어있으면, 반복 종료
//
//            try {
//                FailedTossRequestDto dto = objectMapper.readValue(json, FailedTossRequestDto.class); // json 문자열을 FailedTossRequestDto로 변환
//                tossPaymentClient.retryPaymentConfirm(dto); // Toss 결재 재시도 api 호출
//                log.info("Toss 재시도 성공: {}", dto.idempotencyKey());
//            } catch (Exception e) {
//                log.error("Toss 재시도 실패: {}", json, e);
//                // 재시도 실패시, redis에 재삽입 안 함. 무한 재시도 방지.
//            }
//        }
//    }
}
