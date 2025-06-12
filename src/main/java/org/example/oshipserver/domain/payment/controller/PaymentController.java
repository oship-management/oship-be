package org.example.oshipserver.domain.payment.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentCancelRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentOrderListResponse;
import org.example.oshipserver.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 단건 결제 승인 처리 (Toss 연동)
     */
    @PostMapping("/one-time")
    public ResponseEntity<PaymentConfirmResponse> confirmOneTimePayment(
        @RequestBody PaymentConfirmRequest request
    ) {
        PaymentConfirmResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 다건 결제 승인 처리 (Toss 연동)
     */
    @PostMapping("/multi")
    public ResponseEntity<MultiPaymentConfirmResponse> confirmMultiPayment(
        @RequestBody MultiPaymentConfirmRequest request
    ) {
        MultiPaymentConfirmResponse response = paymentService.confirmMultiPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Toss 기준 결제 조회 (결제상태 확인용)
     */
    @GetMapping("/toss-order/{tossOrderId}")
    public ResponseEntity<PaymentLookupResponse> getPaymentByTossOrderId(@PathVariable String tossOrderId) {
        PaymentLookupResponse response = paymentService.getPaymentByTossOrderId(tossOrderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Toss 기준 결제 조회 (주문 확인용) -> 해당 payment에 연결된 모든 order를 주문리스트로 반환
     */
    @GetMapping("/toss-order/{tossOrderId}/orders")
    public ResponseEntity<List<PaymentOrderListResponse>> getOrdersByTossOrderId(@PathVariable String tossOrderId) {
        List<PaymentOrderListResponse> response = paymentService.getOrdersByTossOrderId(tossOrderId);
        return ResponseEntity.ok(response);
    }


    /**
     * Toss 결제 취소 요청 (전체 취소)
     */
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<String> cancelPayment(
        @PathVariable String paymentKey,
        @RequestBody PaymentCancelRequest request
    ) {
        paymentService.cancelPayment(paymentKey, request.cancelReason());
        return ResponseEntity.ok("결제가 성공적으로 취소되었습니다.");
    }


    /**
     * 내부 주문 기준 결제 조회
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentLookupResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentLookupResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    // 하나의 주문(orderId)에 연결된 모든 결제 조회
    @GetMapping("/orders/{orderId}/payments")
    public ResponseEntity<List<PaymentLookupResponse>> getAllPaymentsByOrderId(@PathVariable Long orderId) {
        List<PaymentLookupResponse> response = paymentService.getAllPaymentsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

}
