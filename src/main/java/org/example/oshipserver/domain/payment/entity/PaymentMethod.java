package org.example.oshipserver.domain.payment.entity;

public enum PaymentMethod {
    CARD,           // 단건 카드 결제용
    VIRTUAL_ACCOUNT // 계좌이체 (거래처 월말 정산용)
}