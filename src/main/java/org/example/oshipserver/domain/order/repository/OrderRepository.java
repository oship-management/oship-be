package org.example.oshipserver.domain.order.repository;

import java.util.Optional;
import org.example.oshipserver.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    boolean existsByOrderNoAndSellerId(String orderNo, Long sellerId);
    boolean existsByOshipMasterNo(String masterNo);

    Optional<Order> findByOshipMasterNo(String masterNo);
}

