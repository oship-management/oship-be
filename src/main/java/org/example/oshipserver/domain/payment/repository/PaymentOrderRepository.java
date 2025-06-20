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

//    // sellerId 기준으로 order에 저장된 결제내역 조회
//    @Query("SELECT DISTINCT po.payment FROM PaymentOrder po " +
//        "WHERE po.order.sellerId = :sellerId")
//    List<Payment> findDistinctPaymentsBySellerId(@Param("sellerId") Long sellerId);

}