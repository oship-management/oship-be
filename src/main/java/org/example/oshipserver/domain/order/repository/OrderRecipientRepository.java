package org.example.oshipserver.domain.order.repository;

import org.example.oshipserver.domain.order.entity.OrderRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRecipientRepository extends JpaRepository<OrderRecipient, Long> {

}
