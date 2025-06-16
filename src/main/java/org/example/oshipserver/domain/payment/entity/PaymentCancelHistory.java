package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 결제 정보 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

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
    public static PaymentCancelHistory create(Payment payment, Integer cancelAmount, String cancelReason) {
        PaymentCancelHistory history = new PaymentCancelHistory();
        history.payment = payment;
        history.cancelAmount = cancelAmount;
        history.cancelReason = cancelReason;
        history.canceledAt = LocalDateTime.now();
        return history;
    }
}
