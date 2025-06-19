package org.example.oshipserver.domain.payment.entity;

public enum PaymentMethod {
    CARD,                    // 하드코딩용
    EASY_PAY_CARD,           // 토스 간편결제 - 카드
    EASY_PAY_ACCOUNT,        // 토스 간편결제 - 토스머니
    VIRTUAL_ACCOUNT          // 계좌이체 (거래처 월말 정산용)
}