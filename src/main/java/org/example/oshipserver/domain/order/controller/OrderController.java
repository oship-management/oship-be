package org.example.oshipserver.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderDeleteRequest;
import org.example.oshipserver.domain.order.dto.response.OrderCreateResponse;
import org.example.oshipserver.domain.order.service.OrderService;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createOrder(
        @Valid @RequestBody OrderCreateRequest orderCreateRequest
    ) {
        String masterNo = orderService.createOrder(orderCreateRequest);
        BaseResponse<OrderCreateResponse> response =
            new BaseResponse<>(201, "주문 생성이 완료되었습니다.", new OrderCreateResponse(masterNo));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 주문 삭제 (Soft Delete)
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteOrder(@PathVariable final Long id,
        @Valid @RequestBody final OrderDeleteRequest request
    ) {
        orderService.softDeleteOrder(id, request.deletedBy());
        return new BaseResponse<>(204, "주문이 성공적으로 삭제되었습니다.", null);
    }

}
