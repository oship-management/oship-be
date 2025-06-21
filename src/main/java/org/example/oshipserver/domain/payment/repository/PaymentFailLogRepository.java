package org.example.oshipserver.domain.payment.repository;

import org.example.oshipserver.domain.payment.entity.PaymentFailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentFailLogRepository extends JpaRepository<PaymentFailLog, Long> {
}
