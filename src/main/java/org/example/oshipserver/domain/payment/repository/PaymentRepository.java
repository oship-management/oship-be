package org.example.oshipserver.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Toss에서 받은 고유 결제 키 중복 여부 확인 (중복 결제 저장 방지용)
    boolean existsByPaymentKey(String paymentKey);

    // paymentNo 생성용 : createdAt 기준으로 오늘 생성된 payment 개수 반환
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // OrderId로 단건조회용
    Optional<Payment> findByTossOrderId(String tossOrderId);

}
