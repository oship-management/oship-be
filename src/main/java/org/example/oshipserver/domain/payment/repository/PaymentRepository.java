package org.example.oshipserver.domain.payment.repository;

import java.time.LocalDateTime;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // paymentNo 생성용 : createdAt 기준으로 오늘 생성된 payment 개수 반환
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
