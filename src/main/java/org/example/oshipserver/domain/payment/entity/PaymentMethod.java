package org.example.oshipserver.domain.payment.entity;

public enum PaymentMethod {
    CARD,           // 카드 결제용 (토스)
    VIRTUAL_ACCOUNT // 계좌이체 (거래처 월말 정산용)
}