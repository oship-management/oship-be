package org.example.oshipserver.domain.payment.repository;

import java.util.List;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCancelHistoryRepository extends JpaRepository<PaymentCancelHistory, Long> {
//    List<PaymentCancelHistory> findByPayment(Payment payment);
    List<PaymentCancelHistory> findByPaymentOrder_Payment(Payment payment);
}
