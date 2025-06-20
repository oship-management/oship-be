package org.example.oshipserver.domain.payment.entity;

import java.util.Set;

public enum PaymentStatus {
    NONE, // 결제 없음
    WAIT, // 결제 대기
    WAIT_CANCEL, // 결제 대기 취소
    COMPLETE, // 승인 완료
    PARTIAL_CANCEL, // 승인 부분 취소
    CANCEL, // 승인 취소
    FAIL; // 승인 실패

    private Set<PaymentStatus> next;

    static {
        NONE.next = Set.of(WAIT, WAIT_CANCEL, COMPLETE, FAIL);
        WAIT.next = Set.of(COMPLETE, FAIL, WAIT_CANCEL);
        WAIT_CANCEL.next = Set.of();
        COMPLETE.next = Set.of(PARTIAL_CANCEL, CANCEL);
        PARTIAL_CANCEL.next = Set.of(CANCEL);
        CANCEL.next = Set.of();
        FAIL.next = Set.of();
    }

    public boolean canTransitionTo(PaymentStatus target) {
        return next.contains(target);
    }
}
