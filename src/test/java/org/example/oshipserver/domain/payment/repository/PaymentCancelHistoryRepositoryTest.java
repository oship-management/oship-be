package org.example.oshipserver.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.testconfig.QuerydslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Import(QuerydslTestConfig.class)  // Querydsltestconfig를 통해 JpaRepository 주입받음
@Testcontainers
@ActiveProfiles("test")
class PaymentCancelHistoryRepositoryTest {

    @Container
    private static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("testuser")
        .withPassword("testpass");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Autowired
    private PaymentCancelHistoryRepository cancelHistoryRepository;

    @Autowired
    private TestEntityManager em; // Order를 저장하기 위함

    @Test
    @DisplayName("결제에 속한 모든 결제취소이력을 정상조회한다")
    void 결제에_속한_모든_결제취소이력을_정상조회한다() {
        // Given
        // 1. 결제 저장
        Payment payment = paymentRepository.save(Payment.builder()
            .paymentKey("test-key")
            .tossOrderId("test-order-id")
            .amount(50000)
            .currency("KRW")
            .sellerId(123L)
            .idempotencyKey("unique-key")
            .paymentNo("PAY-20250702-001")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build());

        // 2. 주문 저장 (필수 필드만 입력)
        Order order = em.persist(Order.builder()
            .orderNo("ORD-20250702-001")
            .oshipMasterNo("OSH-001")
            .weightUnit("kg")
            .parcelCount(1)
            .shipmentActualWeight(new BigDecimal("2.5"))
            .shipmentVolumeWeight(new BigDecimal("3.0"))
            .dimensionWidth(new BigDecimal("10"))
            .dimensionHeight(new BigDecimal("20"))
            .dimensionLength(new BigDecimal("30"))
            .currentStatus(OrderStatus.PENDING)
            .itemContentsType("전자제품")
            .serviceType("일반")
            .packageType("Box")
            .shippingTerm("DDP")
            .lastTrackingEvent("출발지에서 수거됨")
            .sellerId(1L)
            .build());

        // 3. 결제-주문 연결 저장
        PaymentOrder paymentOrder = paymentOrderRepository.save(PaymentOrder.builder()
            .payment(payment)
            .order(order)
            .paymentAmount(30000)
            .paymentStatus(PaymentStatus.COMPLETE)
            .build());

        // 4. 취소 이력 저장 (정적 팩토리 메서드 방식)
        PaymentCancelHistory cancelHistory = PaymentCancelHistory.create(
            paymentOrder,
            10000,
            "사용자 요청"
        );
        cancelHistoryRepository.save(cancelHistory);

        // When : 조회 쿼리 실행
        List<PaymentCancelHistory> result = cancelHistoryRepository.findByPaymentOrder_Payment(payment);

        // Then : 조회된 결과 검증
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCancelReason()).isEqualTo("사용자 요청");
    }
}
