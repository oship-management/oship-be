package org.example.oshipserver.domain.payment.entity;

public enum PaymentMethod {
    CARD,
    SIMPLE_PAY_CARD,         // 토스 간편결제 - 카드
    SIMPLE_PAY_TOSS_MONEY,   // 토스 간편결제 - 토스머니
    VIRTUAL_ACCOUNT // 계좌이체 (거래처 월말 정산용)
}