package org.example.oshipserver.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {


    // tossOrderId 기준으로 결제 리스트로 조회
    List<PaymentOrder> findAllByPayment(Payment payment);

    // 내부 orderId 기준으로 PaymentOrder 조회
    Optional<PaymentOrder> findByOrder_Id(Long orderId);

    // 하나의 주문(orderId)에 연결된 모든 결제 매핑 조회
    List<PaymentOrder> findAllByOrder_Id(Long orderId);

}
