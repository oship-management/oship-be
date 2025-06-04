package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주문 여러 건을 하나의 결제에 묶기 위한 중간 테이블
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    // 여러 개의 PaymentOrder는 하나의 Order를 가리킴 (승인 부분취소용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 주문별 결제 금액
    @Column(nullable = false)
    private Integer paymentAmount;

    // 주문별 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    // 결제 확정일
    private java.time.LocalDateTime confirmedAt;

    // 결제 취소일
    private java.time.LocalDateTime canceledAt;

}