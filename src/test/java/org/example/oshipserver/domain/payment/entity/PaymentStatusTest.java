package org.example.oshipserver.domain.payment.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusTest {

    @Test
    @DisplayName("정상 상태 전이: NONE → COMPLETE")
    void testValidTransitionFromNone() {
        assertTrue(PaymentStatus.NONE.canTransitionTo(PaymentStatus.COMPLETE));
    }

    @Test
    @DisplayName("정상 상태 전이: WAIT → FAIL")
    void testValidTransitionFromWait() {
        assertTrue(PaymentStatus.WAIT.canTransitionTo(PaymentStatus.FAIL));
    }

    @Test
    @DisplayName("정상 상태 전이: COMPLETE → CANCEL")
    void testValidTransitionFromComplete() {
        assertTrue(PaymentStatus.COMPLETE.canTransitionTo(PaymentStatus.CANCEL));
    }

    @Test
    @DisplayName("정상 상태 전이: PARTIAL_CANCEL → CANCEL")
    void testValidTransitionFromPartialCancel() {
        assertTrue(PaymentStatus.PARTIAL_CANCEL.canTransitionTo(PaymentStatus.CANCEL));
    }

    @Test
    @DisplayName("비정상 상태 전이: COMPLETE → WAIT")
    void testInvalidTransition() {
        assertFalse(PaymentStatus.COMPLETE.canTransitionTo(PaymentStatus.WAIT));
    }

    @Test
    @DisplayName("비정상 상태 전이: CANCEL → PARTIAL_CANCEL")
    void testInvalidTransition2() {
        assertFalse(PaymentStatus.CANCEL.canTransitionTo(PaymentStatus.PARTIAL_CANCEL));
    }

}
