package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {

    // db 내부 참조용
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tossOrderId; // Toss에서 전달된 orderId

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

    // 결제 요청자
    @Column(nullable = false)
    private Long sellerId;

    // 우리 서버의 내부 엔티티
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    // 주문 여러건을 묶어서 결제 (다건 결제용)
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentOrder> paymentOrders = new ArrayList<>();

    public void addPaymentOrder(PaymentOrder paymentOrder) {
        this.paymentOrders.add(paymentOrder);
    }

    // 직접 매핑된 주문 리스트만 추출
    public List<Order> getOrders() {
        return paymentOrders.stream()
            .map(PaymentOrder::getOrder)
            .toList();
    }

    private String cardLast4Digits;
    private String receiptUrl;

    public String getCardLast4Digits() {
        return cardLast4Digits;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setCardLast4Digits(String cardLast4Digits) {
        this.cardLast4Digits = cardLast4Digits;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    @Builder
    public Payment(String paymentNo, String paymentKey, String tossOrderId,
        PaymentStatus status, PaymentMethod method, Integer amount,
        String currency, LocalDateTime paidAt, String failReason, Long sellerId,
        String idempotencyKey) {
        this.paymentNo = paymentNo;
        this.paymentKey = paymentKey;
        this.tossOrderId = tossOrderId;
        this.status = status;
        this.method = method;
        this.amount = amount;
        this.currency = currency;
        this.paidAt = paidAt;
        this.failReason = failReason;
        this.sellerId = sellerId;
        this.idempotencyKey = idempotencyKey;
    }

    public void updateStatus(PaymentStatus nextStatus) {
        // 동일 상태로의 전이는 무시
        if (this.status == nextStatus) {
            return;
        }
        if (!this.status.canTransitionTo(nextStatus)) {
            throw new IllegalStateException(
                String.format("현재 상태 '%s'에서는 상태 '%s'로 전이할 수 없습니다.",
                    this.status.name(), nextStatus.name())
            );
        }
        this.status = nextStatus;
    }

    public void partialCancel(int cancelAmount) {
        // 전체 금액보다 많을 수 없도록 제어할 수도 있음
        if (cancelAmount <= 0 || cancelAmount > this.amount) {
            throw new IllegalArgumentException("부분 취소 금액이 유효하지 않습니다.");
        }
        // 추후 이력 관리나 누적 취소 금액 관리로 확장 가능
    }

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

//    // payment > paymentOrder > order르 통해 sellerId 가지고 오는 것보다
//    // payment엔티티에서 결제자 id를 직접 가지고 있도록 연관관계를 맺는게 효율적
//    public void setSellerId(Long sellerId) {
//        this.sellerId = sellerId;
//    }

}