package org.example.oshipserver.domain.order.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.oshipserver.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    boolean existsByOrderNoAndSellerId(String orderNo, Long sellerId);
    boolean existsByOshipMasterNo(String masterNo);

    Page<Order> findBySellerIdAndCreatedAtBetweenAndDeletedFalse(
        Long sellerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Optional<Order> findByOshipMasterNo(String masterNo);

    /**
     * 특정 셀러의 지정된 월에 생성된 주문들을 조회
     *
     *
     - Order ↔ OrderItem: @OneToMany 관계
     *  - order.getOrderItems() 호출 시 Lazy 로딩 발생 → N+1 발생
     *  - 이를 방지하기 위해 fetch join으로 함께 한 번에 조회
     */
    @Query("""
    SELECT DISTINCT o FROM Order o
    LEFT JOIN FETCH o.orderItems
    LEFT JOIN FETCH o.sender
    LEFT JOIN FETCH o.recipient
    WHERE o.sellerId = :sellerId
      AND o.deleted = false
      AND o.createdAt BETWEEN :startOfMonth AND :endOfMonth
""")
    List<Order> findBySellerIdAndCreatedAtBetween(
        @Param("sellerId") Long sellerId,
        @Param("startOfMonth") LocalDateTime startOfMonth,
        @Param("endOfMonth") LocalDateTime endOfMonth
    );

}

