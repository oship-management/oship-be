package org.example.oshipserver.domain.notification.entity;

public enum NotificationType {
    ORDER_CREATED,     // 주문 생성 완료
    ORDER_CANCELLED,   // 주문 취소
    PAYMENT_COMPLETED, // 결제 완료
    PAYMENT_FAILED,    // 결제 실패
    DELIVERY_STARTED,  // 배송 시작
    DELIVERY_COMPLETED // 배송 완료
}