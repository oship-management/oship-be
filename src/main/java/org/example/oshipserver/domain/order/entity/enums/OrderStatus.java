package org.example.oshipserver.domain.order.entity.enums;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum OrderStatus {
    PENDING(null),
    PAID(null),
    FAILED(null),
    CANCELLED(null),
    REFUNDED(null);

    private Set<OrderStatus> next;

    OrderStatus(Set<OrderStatus> next) {
        this.next = next;
    }

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

    public void assertTransitionTo(OrderStatus target) {
        if (!canTransitionTo(target)) {
            log.warn("[OrderStatus 전이 실패] {} → {} 는 허용되지 않는 상태 전이입니다", this, target);
            throw new IllegalStateException("주문 상태를 " + this + "에서 " + target + "로 전이할 수 없습니다.");
        }
    }
}