package org.example.oshipserver.domain.payment.entity;

import java.util.Set;

public enum PaymentStatus {
    NONE(null), // 결제 없음
    WAIT(null), // 결제 대기
    WAIT_CANCEL(null), // 결제 대기 취소
    COMPLETE(null), // 승인 완료
    PARTIAL_CANCEL(null), // 승인 부분 취소
    CANCEL(null), // 승인 취소
    FAIL(null); // 승인 실패

    private Set<PaymentStatus> next;

    PaymentStatus(Set<PaymentStatus> next) {
        this.next = next;
    }

    static {
        NONE.next = Set.of(WAIT, WAIT_CANCEL, COMPLETE, FAIL);
        WAIT.next = Set.of(COMPLETE, FAIL, WAIT_CANCEL);
        WAIT_CANCEL.next = Set.of(COMPLETE, FAIL);
        COMPLETE.next = Set.of(PARTIAL_CANCEL, CANCEL);
        PARTIAL_CANCEL.next = Set.of(CANCEL);
        CANCEL.next = Set.of();
        FAIL.next = Set.of(WAIT,WAIT_CANCEL, COMPLETE, PARTIAL_CANCEL, CANCEL);
    }

    public boolean canTransitionTo(PaymentStatus target) {
        return next.contains(target);
    }
}
