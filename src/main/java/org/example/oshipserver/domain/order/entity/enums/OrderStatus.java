package org.example.oshipserver.domain.order.entity.enums;

import java.util.Set;

public enum OrderStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED,
    REFUNDED;

    private Set<OrderStatus> next;

    static {
        PENDING.next = Set.of(PAID, CANCELLED, FAILED);
        PAID.next = Set.of(CANCELLED, REFUNDED);
        FAILED.next = Set.of(PAID);
        CANCELLED.next = Set.of(REFUNDED);
        REFUNDED.next = Set.of();
    }

    public boolean canTransitionTo(OrderStatus target) {
        return next.contains(target);
    }
}

