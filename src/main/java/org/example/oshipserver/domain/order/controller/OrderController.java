package org.example.oshipserver.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.aop.OrderExecutionLog;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderUpdateRequest;
import org.example.oshipserver.domain.order.dto.response.OrderCreateResponse;
import org.example.oshipserver.domain.order.dto.response.OrderDetailResponse;
import org.example.oshipserver.domain.order.dto.response.OrderListResponse;
import org.example.oshipserver.domain.order.service.OrderService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.example.oshipserver.global.common.response.PageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @OrderExecutionLog
    @PostMapping
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createOrder(
        @Valid @RequestBody OrderCreateRequest orderCreateRequest
    ) {
        String masterNo = orderService.createOrder(orderCreateRequest);
        BaseResponse<OrderCreateResponse> response =
            new BaseResponse<>(201, "주문 생성이 완료되었습니다.", new OrderCreateResponse(masterNo));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public BaseResponse<PageResponseDto<OrderListResponse>> getOrderList(
        @RequestParam(required = false) Long sellerId,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        Pageable pageable
    ) {
        PageResponseDto<OrderListResponse> response = orderService.getOrderList(sellerId, startDate, endDate, pageable);
        return new BaseResponse<>(200, "주문 목록 조회 성공", response);
    }


    @GetMapping("/{id}")
    public BaseResponse<OrderDetailResponse> getOrderDetail(@PathVariable final Long id) {
        return new BaseResponse<>(200, "주문 상세 조회 성공", orderService.getOrderDetail(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateOrder(
        @PathVariable Long id,
        @Valid @RequestBody OrderUpdateRequest request
    ) {
        orderService.updateOrder(id, request);
        return ResponseEntity.ok(new BaseResponse<>(200, "주문 정보가 수정되었습니다.", null));
    }

    /**
     * 주문 삭제 (Soft Delete)
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteOrder(@PathVariable final Long id) {
        orderService.softDeleteOrder(id);
        return new BaseResponse<>(204, "주문이 성공적으로 삭제되었습니다.", null);
    }

}