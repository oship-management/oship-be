package org.example.oshipserver.domain.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
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
     * Toss 기준 결제 조회
     */
    @GetMapping("/orders/{tossOrderId}")
    public ResponseEntity<PaymentLookupResponse> getPaymentByTossOrderId(@PathVariable String tossOrderId) {
        PaymentLookupResponse response = paymentService.getPaymentByTossOrderId(tossOrderId);
        return ResponseEntity.ok(response);
    }

}
