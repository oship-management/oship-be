package org.example.oshipserver.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.service.stats.OrderStatsCacheFacade;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class OrderStatsController {

    private final OrderStatsCacheFacade cacheFacade;

    @GetMapping("/v1/seller-stats/monthly")
    public ResponseEntity<BaseResponse<OrderStatsResponse>> getMonthlyStats(
        Authentication authentication,
        @RequestParam String month
    ) {
        Long userId = Long.valueOf(authentication.getName());
        OrderStatsResponse response = cacheFacade.getMonthlyStats(userId, month);
        return ResponseEntity.ok(new BaseResponse<>(200, "셀러 주문 통계 조회 성공", response));
    }

    @GetMapping("/v2/seller-stats/monthly")
    public ResponseEntity<BaseResponse<OrderStatsResponse>> getV2(
        Authentication authentication,
        @RequestParam String month
    ) {
        Long userId = Long.valueOf(authentication.getName());
        OrderStatsResponse result = cacheFacade.getMonthlyStatsV2(userId, month);
        return ResponseEntity.ok(new BaseResponse<>(200, "v2 - local 캐시 통계 조회 성공", result));
    }

    @GetMapping("/v3/seller-stats/monthly")
    public ResponseEntity<BaseResponse<OrderStatsResponse>> getV3(
        Authentication authentication,
        @RequestParam String month
    ) {
        Long userId = Long.valueOf(authentication.getName());
        OrderStatsResponse result = cacheFacade.getMonthlyStatsV3(userId, month);
        return ResponseEntity.ok(new BaseResponse<>(200, "v3 - redis 캐시 통계 조회 성공", result));
    }
}

