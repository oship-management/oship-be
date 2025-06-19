package org.example.oshipserver.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Toss에서 받은 고유 결제 키 중복 여부 확인 (중복 결제 저장 방지용)
    boolean existsByPaymentKey(String paymentKey);

    // paymentNo 생성용 : createdAt 기준으로 오늘 생성된 payment 개수 반환
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // tossOrderId로 조회(toss 기준의 결제 단위 조회)
    Optional<Payment> findByTossOrderId(String tossOrderId);

    // tossPaymentKey로 조회 (결제 취소용)
    Optional<Payment> findByPaymentKey(String paymentKey);

//    // sellerId 기준으로 결제 조회
//    List<Payment> findAllBySellerId(Long sellerId);

    // 날짜 + sellerId 기준으로 결제 조회
    List<Payment> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end);

}
