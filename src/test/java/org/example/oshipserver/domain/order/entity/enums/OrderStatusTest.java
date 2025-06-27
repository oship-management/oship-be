package org.example.oshipserver.domain.order.entity.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    @DisplayName("정상 상태 전이: PENDING → PAID")
    void testValidTransition() {
        OrderStatus from = OrderStatus.PENDING;
        OrderStatus to = OrderStatus.PAID;

        assertDoesNotThrow(() -> from.assertTransitionTo(to));
        assertTrue(from.canTransitionTo(to));
    }

    @Test
    @DisplayName("정상 상태 전이: CANCELLED → REFUNDED")
    void testValidTransition2() {
        assertTrue(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.REFUNDED));
    }

    @Test
    @DisplayName("허용되지 않는 상태 전이: PAID → FAILED")
    void testInvalidTransition() {
        OrderStatus from = OrderStatus.PAID;
        OrderStatus to = OrderStatus.FAILED;

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> from.assertTransitionTo(to));

        assertEquals("주문 상태를 PAID에서 FAILED로 전이할 수 없습니다.", exception.getMessage());
        assertFalse(from.canTransitionTo(to));
    }

    @Test
    @DisplayName("허용되지 않는 상태 전이: REFUNDED → PENDING")
    void testInvalidTransition2() {
        assertFalse(OrderStatus.REFUNDED.canTransitionTo(OrderStatus.PENDING));
    }
}
