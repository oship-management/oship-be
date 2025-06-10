package org.example.oshipserver.domain.order.repository;

import java.time.LocalDateTime;
import org.example.oshipserver.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    boolean existsByOrderNoAndSellerId(String orderNo, Long sellerId);
    boolean existsByOshipMasterNo(String masterNo);
    Page<Order> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}

