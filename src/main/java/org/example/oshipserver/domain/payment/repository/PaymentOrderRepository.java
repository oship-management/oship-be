package org.example.oshipserver.domain.payment.repository;

import java.util.List;
import java.util.Optional;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {


    // 하나의 결제(Payment)에 연결된 모든 주문 매핑(PaymentOrder)을 조회
    List<PaymentOrder> findAllByPayment_Id(Long paymentId);

}