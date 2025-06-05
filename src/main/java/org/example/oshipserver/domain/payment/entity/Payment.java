package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    // db 내부 참조용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 프론트나 toss 연동용
    // 우리 서버에서 생성한 고유 결제 번호 (예: "PAY-20250602-0001")
    @Column(nullable = false, unique = true)
    private String paymentNo;

    // Toss에서 전달해주는 고유 키
    @Column(nullable = true, unique = true)
    private String paymentKey;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 결제 수단
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    // 결제 금액
    @Column(nullable = false)
    private Integer amount;

    // 통화
    @Column(nullable = false)
    private String currency;

    // 결제 승인 시간 (Toss에서 approvedAt 내려줌)
    private LocalDateTime paidAt;

    // 결제 실패 사유
    private String failReason;

    @Column(nullable = false)
    private String orderId;

    // 주문 여러건을 묶어서 결제
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentOrder> orders = new ArrayList<>();


    @Builder
    public Payment(String paymentNo, String paymentKey, String orderId,
        PaymentStatus status, PaymentMethod method, Integer amount,
        String currency, LocalDateTime paidAt, String failReason) {
        this.paymentNo = paymentNo;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.paidAt = paidAt;
        this.failReason = failReason;
    }

    public void markSuccess(String paymentKey, LocalDateTime paidAt) {
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.COMPLETE;
        this.paidAt = paidAt;
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAIL;
        this.failReason = reason;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCEL;
    }
}