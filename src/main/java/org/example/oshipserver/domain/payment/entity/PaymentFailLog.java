package org.example.oshipserver.domain.payment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payment_fail_logs")
public class PaymentFailLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실패한 요청 URL (ex. Toss 결제 승인 요청 URL)
    @Column(nullable = false, length = 500)
    private String url;

    // 실패한 요청 바디 (JSON 문자열 형태로 직렬화)
    @Lob
    @Column(nullable = false)
    private String requestBody;

    // Idempotency-Key 값
    @Column(nullable = false, length = 100)
    private String idempotencyKey;

    // 실패 사유 또는 에러 메시지
    @Column(nullable = false, length = 1000)
    private String errorMessage;

    private LocalDateTime failedAt;

    @PrePersist
    protected void onPrePersist() {
        if (this.failedAt == null) {
            this.failedAt = LocalDateTime.now();
        }
    }
}
