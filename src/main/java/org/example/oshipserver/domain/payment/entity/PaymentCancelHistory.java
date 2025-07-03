package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_cancel_histories")
public class PaymentCancelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제-주문 중간 테이블과의 연관관계 (payment, order 각각 연결하는 대신)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    private PaymentOrder paymentOrder;

    // 부분취소한 금액
    @Column(nullable = false)
    private Integer cancelAmount;

    // 취소 사유
    @Column(nullable = false)
    private String cancelReason;

    // 취소한 시각
    @Column(nullable = false)
    private LocalDateTime canceledAt;

    // 정적 팩토리 메서드
    @Builder
    public static PaymentCancelHistory create(PaymentOrder paymentOrder, Integer cancelAmount, String cancelReason) {
        PaymentCancelHistory history = new PaymentCancelHistory();
        history.paymentOrder = paymentOrder;
        history.cancelAmount = cancelAmount;
        history.cancelReason = cancelReason;
        history.canceledAt = LocalDateTime.now();
        return history;
    }
}
