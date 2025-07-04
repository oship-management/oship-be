package org.example.oshipserver.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.testconfig.QuerydslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@DataJpaTest
@Import(QuerydslTestConfig.class)
@Testcontainers
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
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

    @Test
    @DisplayName("paymentKey 중복 여부를 확인한다")
    void paymentKey로_중복여부확인() {
        // given
        Payment saved = paymentRepository.save(Payment.builder()
            .paymentKey("unique-key-001")
            .tossOrderId("toss-001")
            .paymentNo("PAY-20250702-003")
            .amount(10000)
            .currency("KRW")
            .sellerId(1L)
            .idempotencyKey("idempotent-001")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build());

        // when
        boolean exists = paymentRepository.existsByPaymentKey("unique-key-001");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("createdAt 기준으로 최근 5분 내 생성된 결제 건수를 조회한다")
    void createdAt_기준으로_최근_5분내_결제건수조회() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusMinutes(5);
        LocalDateTime end = now.plusMinutes(5);

        Payment payment1 = Payment.builder()
            .paymentKey("key-1")
            .tossOrderId("toss-1")
            .paymentNo("PAY-1")
            .amount(20000)
            .currency("KRW")
            .sellerId(10L)
            .idempotencyKey("idemp-1")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment1, "createdAt", now);

        Payment payment2 = Payment.builder()
            .paymentKey("key-2")
            .tossOrderId("toss-2")
            .paymentNo("PAY-2")
            .amount(30000)
            .currency("KRW")
            .sellerId(10L)
            .idempotencyKey("idemp-2")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment2, "createdAt", now);

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);

        // when
        int count = paymentRepository.countByCreatedAtBetween(start, end);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("idempotencyKey로 결제를 조회한다")
    void idempotencyKey로_조회() {
        // given
        paymentRepository.save(Payment.builder()
            .paymentKey("key-3")
            .tossOrderId("toss-3")
            .paymentNo("PAY-3")
            .amount(40000)
            .currency("KRW")
            .sellerId(20L)
            .idempotencyKey("idemp-3")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build());

        // when
        Optional<Payment> result = paymentRepository.findByIdempotencyKey("idemp-3");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTossOrderId()).isEqualTo("toss-3");
    }

    @Test
    @DisplayName("sellerId와 날짜 범위로 결제를 조회한다")
    void sellerId와_날짜범위로_조회() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Long sellerId = 99L;

        Payment payment = Payment.builder()
            .paymentKey("key-4")
            .tossOrderId("toss-4")
            .paymentNo("PAY-4")
            .amount(15000)
            .currency("KRW")
            .sellerId(sellerId)
            .idempotencyKey("idemp-4")
            .method(PaymentMethod.EASY_PAY_CARD)
            .status(PaymentStatus.COMPLETE)
            .build();

        ReflectionTestUtils.setField(payment, "createdAt", now);
        paymentRepository.save(payment);

        // when
        List<Payment> result = paymentRepository.findBySellerIdAndCreatedAtBetween(
            sellerId, now.minusDays(1), now.plusDays(1));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPaymentKey()).isEqualTo("key-4");
    }
}
