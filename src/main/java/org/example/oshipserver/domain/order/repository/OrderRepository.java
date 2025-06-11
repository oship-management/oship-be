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
    Page<Order> findBySellerIdAndCreatedAtBetween(Long sellerId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Optional<Order> findByOshipMasterNo(String masterNo);

    /**
     * 특정 셀러의 지정된 월에 생성된 주문들을 조회
     *
     *
     * @param sellerId 셀러의 고유 ID
     * @param startOfMonth 조회 시작일
     * @param endOfMonth   조회 종료일
     * @return 조건에 부합하는 주문 목록
     */
    @Query("""
    SELECT o FROM Order o
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

