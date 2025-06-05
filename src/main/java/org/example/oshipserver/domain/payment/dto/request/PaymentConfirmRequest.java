package org.example.oshipserver.domain.payment.dto.request;

/**
 * 결제 승인 요청 DTO (Toss 결제 승인 API 요청용)
 */
public record PaymentConfirmRequest(
    String paymentKey,   // Toss에서 전달해주는 결제 고유 키
    String orderId,      // 우리 서버에서 생성한 주문 ID
    Integer amount       // (임시) 프론트 입력값. 나중엔 서버에서 계산하여 받아올 것 (이 필드를 제거하고, 서비스로직에서 orderId로 조회하여 amount 계산하여 Toss로 confirm 전달하도록)
) {}