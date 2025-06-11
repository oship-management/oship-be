package org.example.oshipserver.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {


    // 하나의 결제(Payment)에 연결된 모든 주문 매핑(PaymentOrder)을 조회
    List<PaymentOrder> findAllByPayment_Id(Long paymentId);

    // 내부 orderId 기준으로 PaymentOrder 조회
    Optional<PaymentOrder> findByOrder_Id(Long orderId);

    // 하나의 주문(orderId)에 연결된 모든 PaymentOrder 조회
    List<PaymentOrder> findAllByOrder_Id(Long orderId);

}
