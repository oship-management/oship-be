package org.example.oshipserver.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.response.OrderStatsResponse;
import org.example.oshipserver.domain.order.service.OrderStatsService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderStatsController {

    private final OrderStatsService orderStatsService;

    @GetMapping("/v1/seller-stats/monthly")
    public ResponseEntity<BaseResponse<OrderStatsResponse>> getMonthlyStats(
        @RequestParam Long sellerId,
        @RequestParam String month
    ) {
        OrderStatsResponse response = orderStatsService.getMonthlyStats(sellerId, month);
        return ResponseEntity.ok(new BaseResponse<>(200, "셀러 주문 통계 조회 성공", response));
    }
}

