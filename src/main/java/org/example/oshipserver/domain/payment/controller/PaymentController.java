package org.example.oshipserver.domain.payment.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentFullCancelRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentPartialCancelRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentCancelHistoryResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.UserPaymentLookupResponse;
import org.example.oshipserver.domain.payment.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

//    /**
//     * Toss 기준 결제 조회 (결제상태 확인용)
//     */
//    @GetMapping("/toss-order/{tossOrderId}")
//    public ResponseEntity<PaymentLookupResponse> getPaymentByTossOrderId(@PathVariable String tossOrderId) {
//        PaymentLookupResponse response = paymentService.getPaymentByTossOrderId(tossOrderId);
//        return ResponseEntity.ok(response);
//    }
//
//    /**
//     * Toss 기준 결제 조회 (주문 확인용) -> 해당 payment에 연결된 모든 order를 주문리스트로 반환
//     */
//    @GetMapping("/toss-order/{tossOrderId}/orders")
//    public ResponseEntity<List<PaymentOrderListResponse>> getOrdersByTossOrderId(@PathVariable String tossOrderId) {
//        List<PaymentOrderListResponse> response = paymentService.getOrdersByTossOrderId(tossOrderId);
//        return ResponseEntity.ok(response);
//    }

    /**
     * Toss 결제 전체 취소 요청
     */
    @PostMapping("/{paymentKey}/cancel/full")
    public ResponseEntity<String> cancelFullPayment(
        @PathVariable String paymentKey,
        @RequestBody PaymentFullCancelRequest request
    ) {
        paymentService.cancelFullPayment(paymentKey, request.cancelReason());
        return ResponseEntity.ok("전체 취소 완료");
    }

    /**
     * Toss 결제 부분 취소 요청
     */
    @PostMapping("/{paymentKey}/cancel/partial")
    public ResponseEntity<String> cancelPartialPayment(
        @PathVariable String paymentKey,
        @RequestBody PaymentPartialCancelRequest request
    ) {
        paymentService.cancelPartialPayment(paymentKey, request.orderId(), request.cancelAmount(), request.cancelReason());
        return ResponseEntity.ok("부분 취소 완료");
    }

    /**
     * Toss 결제 취소 이력 조회
     */
    @GetMapping("/{paymentKey}/cancel-history")
    public ResponseEntity<List<PaymentCancelHistoryResponse>> getCancelHistory(
        @PathVariable String paymentKey
    ) {
        List<PaymentCancelHistoryResponse> histories = paymentService.getCancelHistory(paymentKey);
        return ResponseEntity.ok(histories);
    }

    /**
     * sellerId 기준 결제 내역 조회
     * 추후에 관리자만 조회하도록 수정
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<PaymentLookupResponse>> getPaymentsBySeller(
        @PathVariable Long sellerId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<PaymentLookupResponse> response = paymentService.getPaymentsBySellerId(sellerId, startDate, endDate);
        return ResponseEntity.ok(response);
    }


    /**
     * 사용자 본인의 결제 내역 조회
     * 토큰에서 seller 정보를 추출하는 방식
     */
    @GetMapping("/mypayments")
    public ResponseEntity<List<UserPaymentLookupResponse>> getMyPayments(
        @AuthenticationPrincipal CustomUserDetail userDetail,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {

        Long userId = Long.valueOf(userDetail.getUserId());
        List<UserPaymentLookupResponse> response = paymentService.getPaymentsByUser(userId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

}