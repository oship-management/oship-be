package org.example.oshipserver.domain.payment.entity;

public enum PaymentStatus {
    NONE,  // 결제 없음
    WAIT,  // 결제 대기
    COMPLETE,  // 승인 완료
    PARTIAL_CANCEL,  // 승인 부분 취소
    CANCEL,  // 승인 취소
    FAIL,  // 승인 실패
    WAIT_CANCEL;  // 결제 대기 취소
}