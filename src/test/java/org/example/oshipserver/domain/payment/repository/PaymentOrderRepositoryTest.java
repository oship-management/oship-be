package org.example.oshipserver.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.payment.entity.Payment;
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
@Import(QuerydslTestConfig.class)
@Testcontainers
@ActiveProfiles("test")
class PaymentOrderRepositoryTest {

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
    private TestEntityManager em;

    @Test
    @DisplayName("결제 ID로 연결된 모든 PaymentOrder 조회")
    void 결제ID로_연결된_모든_PaymentOrder_조회() {
        // Given
        Payment payment = paymentRepository.save(Payment.builder()
            .paymentKey("test-key-2")
            .tossOrderId("toss-order-id-2")
            .amount(70000)
            .currency("KRW")
            .sellerId(456L)
            .idempotencyKey("unique-key-2")
            .paymentNo("PAY-20250702-002")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build());

        Order order1 = em.persist(Order.builder()
            .orderNo("ORD-20250702-002")
            .oshipMasterNo("OSH-002")
            .weightUnit("kg")
            .parcelCount(1)
            .shipmentActualWeight(new BigDecimal("1.0"))
            .shipmentVolumeWeight(new BigDecimal("1.5"))
            .dimensionWidth(new BigDecimal("10"))
            .dimensionHeight(new BigDecimal("20"))
            .dimensionLength(new BigDecimal("30"))
            .currentStatus(OrderStatus.PENDING)
            .itemContentsType("도서")
            .serviceType("일반")
            .packageType("Envelope")
            .shippingTerm("DAP")
            .lastTrackingEvent("배송 준비 완료")
            .sellerId(2L)
            .build());

        Order order2 = em.persist(Order.builder()
            .orderNo("ORD-20250702-003")
            .oshipMasterNo("OSH-003")
            .weightUnit("kg")
            .parcelCount(2)
            .shipmentActualWeight(new BigDecimal("2.0"))
            .shipmentVolumeWeight(new BigDecimal("2.5"))
            .dimensionWidth(new BigDecimal("15"))
            .dimensionHeight(new BigDecimal("25"))
            .dimensionLength(new BigDecimal("35"))
            .currentStatus(OrderStatus.PENDING)
            .itemContentsType("의류")
            .serviceType("퀵")
            .packageType("Bag")
            .shippingTerm("FOB")
            .lastTrackingEvent("집화 완료")
            .sellerId(2L)
            .build());

        paymentOrderRepository.save(PaymentOrder.builder()
            .payment(payment)
            .order(order1)
            .paymentAmount(30000)
            .paymentStatus(PaymentStatus.COMPLETE)
            .build());

        paymentOrderRepository.save(PaymentOrder.builder()
            .payment(payment)
            .order(order2)
            .paymentAmount(40000)
            .paymentStatus(PaymentStatus.COMPLETE)
            .build());

        // When
        List<PaymentOrder> results = paymentOrderRepository.findAllByPayment_Id(payment.getId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("order").extracting("orderNo")
            .containsExactlyInAnyOrder("ORD-20250702-002", "ORD-20250702-003");
    }
}
