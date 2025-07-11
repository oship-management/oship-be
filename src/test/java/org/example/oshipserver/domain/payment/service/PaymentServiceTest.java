package org.example.oshipserver.domain.payment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.example.oshipserver.client.toss.IdempotentRestClient;
import org.example.oshipserver.client.toss.TossPaymentClient;
import org.example.oshipserver.domain.order.dto.response.OrderPaymentResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest.MultiOrderRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentCancelHistoryResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.UserPaymentLookupResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.repository.PaymentCancelHistoryRepository;
import org.example.oshipserver.domain.payment.repository.PaymentOrderRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private IdempotentRestClient idempotentRestClient;
    @Mock
    private TossPaymentClient tossPaymentClient;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private PaymentCancelHistoryRepository paymentCancelHistoryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PaymentNotificationService paymentNotificationService;


    @Test
    @DisplayName("이미 처리된 paymentKey일 경우 예외가 발생한다")
    void confirmPayment_이미존재하는PaymentKey는예외() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "paymentKey123", 10L, "tossOrderId123", 50000
        );

        given(paymentRepository.existsByPaymentKey("paymentKey123")).willReturn(true);

        // when & then
        ApiException exception = catchThrowableOfType(
            () -> paymentService.confirmPayment(request),
            ApiException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).contains("이미 처리된 결제입니다.");
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.DUPLICATED_PAYMENT);
    }

    @Test
    @DisplayName("결제 승인 요청이 정상적으로 처리되면 PaymentConfirmResponse를 반환한다")
    void confirmPayment_성공() {
        // given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
            "payKey123", 1L, "tossOrder123", 50000
        );

        TossPaymentConfirmResponse tossResponse = new TossPaymentConfirmResponse();
        tossResponse.setOrderId("tossOrder123");
        tossResponse.setPaymentKey("payKey123");
        tossResponse.setTotalAmount(50000);
        tossResponse.setCurrency("KRW");
        tossResponse.setStatus("DONE");
        tossResponse.setApprovedAt("2025-07-02T15:00:00+09:00");

        TossPaymentConfirmResponse.Card card = new TossPaymentConfirmResponse.Card();
        card.setNumber("1234567812345678");
        tossResponse.setCard(card);
        tossResponse.setMethod("카드");

        TossPaymentConfirmResponse.Receipt receipt = new TossPaymentConfirmResponse.Receipt();
        receipt.setUrl("https://toss.com/receipt");
        tossResponse.setReceipt(receipt);

        Order order = Order.builder()
            .sellerId(99L)
            .currentStatus(OrderStatus.PENDING)
            .build();
        ReflectionTestUtils.setField(order, "id", 1L);

        given(paymentRepository.existsByPaymentKey("payKey123")).willReturn(false);
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));
        given(idempotentRestClient.postForIdempotent(
            anyString(), anyMap(), eq(TossPaymentConfirmResponse.class), anyString())
        ).willReturn(tossResponse);

        // when
        PaymentConfirmResponse response = paymentService.confirmPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status().name()).isEqualTo("COMPLETE");
        assertThat(response.paymentKey()).isEqualTo("payKey123");
        assertThat(response.tossOrderId()).isEqualTo("tossOrder123");
        assertThat(response.cardLast4Digits()).isEqualTo("5678");
    }

    @Test
    @DisplayName("다건 결제 승인 요청이 정상적으로 처리되면 MultiPaymentConfirmResponse를 반환한다")
    void confirmMultiPayment_성공() {
        // given
        List<MultiOrderRequest> orderRequests = List.of(
            new MultiOrderRequest(201L, 30000),
            new MultiOrderRequest(202L, 20000)
        );

        MultiPaymentConfirmRequest request = new MultiPaymentConfirmRequest(
            "tgen_20250612100748JLbH3", "MC7492581642834", orderRequests, "KRW"
        );

        TossPaymentConfirmResponse tossResponse = new TossPaymentConfirmResponse();
        tossResponse.setOrderId("MC7492581642834");
        tossResponse.setPaymentKey("tgen_20250612100748JLbH3");
        tossResponse.setCurrency("KRW");
        tossResponse.setTotalAmount(50000);
        tossResponse.setStatus("DONE");
        tossResponse.setApprovedAt("2025-06-12T10:08:43+09:00");
        tossResponse.setMethod("카드");

        TossPaymentConfirmResponse.Card card = new TossPaymentConfirmResponse.Card();
        card.setNumber("1234567812345953");
        tossResponse.setCard(card);

        TossPaymentConfirmResponse.Receipt receipt = new TossPaymentConfirmResponse.Receipt();
        receipt.setUrl(
            "https://dashboard.tosspayments.com/receipt/redirection?transactionId=tgen_20250612100748JLbH3&ref=PX");
        tossResponse.setReceipt(receipt);

        Order order1 = Order.builder()
            .sellerId(1L)
            .currentStatus(OrderStatus.PENDING)
            .build();
        ReflectionTestUtils.setField(order1, "id", 201L);

        Order order2 = Order.builder()
            .sellerId(1L)
            .currentStatus(OrderStatus.PENDING)
            .build();
        ReflectionTestUtils.setField(order2, "id", 202L);

        given(paymentRepository.existsByPaymentKey("tgen_20250612100748JLbH3")).willReturn(false);
        given(orderRepository.findById(201L)).willReturn(Optional.of(order1));
        given(orderRepository.findById(202L)).willReturn(Optional.of(order2));
        given(idempotentRestClient.postForIdempotent(
            anyString(), anyMap(), eq(TossPaymentConfirmResponse.class), anyString())
        ).willReturn(tossResponse);

        // when
        MultiPaymentConfirmResponse response = paymentService.confirmMultiPayment(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.paymentKey()).isEqualTo("tgen_20250612100748JLbH3");
        assertThat(response.paymentStatus().name()).isEqualTo("COMPLETE"); // PaymentStatus Enum 기준
        assertThat(response.approvedAt()).isEqualTo("2025-06-12T10:08:43+09:00");
        assertThat(response.totalAmount()).isEqualTo(50000);
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.cardLast4Digits()).isEqualTo("5953");
        assertThat(response.receiptUrl()).contains("dashboard.tosspayments.com/receipt");
        assertThat(response.orderIds()).containsExactly("201", "202");
    }

    @Test
    @DisplayName("전체 결제 취소 요청이 성공하면 Payment 상태가 CANCEL로 업데이트된다")
    void cancelFullPayment_성공() {
        // given
        String paymentKey = "payKey123";
        String cancelReason = "사용자 요청";

        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentOrder po1 = mock(PaymentOrder.class);
        PaymentOrder po2 = mock(PaymentOrder.class);

        Order order1 = Order.builder().currentStatus(OrderStatus.PENDING).build();
        Order order2 = Order.builder().currentStatus(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order1, "id", 101L);
        ReflectionTestUtils.setField(order2, "id", 102L);

        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)).willReturn(
            List.of());
        given(paymentOrderRepository.findAllByPayment_Id(payment.getId())).willReturn(
            List.of(po1, po2));

        given(po1.getOrder()).willReturn(order1);
        given(po1.getPaymentAmount()).willReturn(30000);
        given(po2.getOrder()).willReturn(order2);
        given(po2.getPaymentAmount()).willReturn(20000);

        // when
        paymentService.cancelFullPayment(paymentKey, cancelReason);

        // then
        verify(tossPaymentClient).requestCancel(eq(paymentKey), eq(cancelReason), eq(50000));
        verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.CANCEL));
        verify(orderRepository).save(order1);
        verify(orderRepository).save(order2);
        verify(paymentCancelHistoryRepository, times(2)).save(any(PaymentCancelHistory.class));
    }

    @Test
    @DisplayName("부분 결제 취소 요청이 성공하면 Payment 상태가 PARTIAL_CANCEL로 변경된다")
    void cancelPartialPayment_성공() {
        // given
        String paymentKey = "payKey123";
        Long orderId = 1001L;
        String cancelReason = "사용자 요청";

        // 결제 엔티티
        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        // 주문 + 매핑 객체
        Order order = Order.builder().currentStatus(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order, "id", orderId);

        PaymentOrder paymentOrder = mock(PaymentOrder.class);
        given(paymentOrder.getOrder()).willReturn(order);
        given(paymentOrder.getPaymentStatus()).willReturn(PaymentStatus.COMPLETE);
        given(paymentOrder.getPaymentAmount()).willReturn(20000);

        List<PaymentOrder> allOrders = List.of(paymentOrder);
        ReflectionTestUtils.setField(payment, "paymentOrders", allOrders);

        // 취소 이력 - 누적 취소 금액 20000원
        PaymentCancelHistory cancelHistory = mock(PaymentCancelHistory.class);
        given(cancelHistory.getCancelAmount()).willReturn(20000);

        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)).willReturn(
            List.of(cancelHistory));

        // when
        paymentService.cancelPartialPayment(paymentKey, orderId, cancelReason);

        // then
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 20000);
        verify(paymentRepository).save(payment);
        verify(paymentOrderRepository).save(paymentOrder);
        verify(paymentCancelHistoryRepository).save(any(PaymentCancelHistory.class));
    }

    @Test
    @DisplayName("부분 취소 후 전체 취소가 남은 금액에 대해 정상 처리된다")
    void cancelPartial_thenCancelFullPayment_성공() {
        // given
        String paymentKey = "payKey123";
        String cancelReason = "사용자 요청";

        // 주문 2건 생성
        Order order1 = Order.builder().currentStatus(OrderStatus.PENDING).build(); // 부분 취소 대상
        Order order2 = Order.builder().currentStatus(OrderStatus.PENDING).build(); // 전체 취소 대상
        ReflectionTestUtils.setField(order1, "id", 1L);
        ReflectionTestUtils.setField(order2, "id", 2L);

        // Payment 생성
        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000)
            .status(PaymentStatus.COMPLETE)
            .method(PaymentMethod.CARD)
            .currency("KRW")
            .sellerId(99L)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        // PaymentOrder 생성
        PaymentOrder po1 = PaymentOrder.of(payment, order1, 20000);
        PaymentOrder po2 = PaymentOrder.of(payment, order2, 30000);
        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2));

        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
        given(paymentOrderRepository.findAllByPayment_Id(payment.getId()))
            .willReturn(List.of(po1, po2));

        // 부분 취소 이후 누적 이력: 20000
        PaymentCancelHistory h1 = mock(PaymentCancelHistory.class);
        given(h1.getCancelAmount()).willReturn(20000);

        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment))
            .willReturn(List.of())           // 1차 취소 시 (0)
            .willReturn(List.of(h1))         // 2차 전체 취소 직전 누적 (20000)
            .willReturn(List.of(h1));        // 2차 전체 취소 직후에도 여전히 h1만 반환되도록 가정 (mock이므로 괜찮음)

        // when
        paymentService.cancelPartialPayment(paymentKey, 1L, cancelReason); // 주문 1 취소
        paymentService.cancelFullPayment(paymentKey, cancelReason);        // 주문 2 전체 취소

        // then
        // Toss 요청 검증
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 20000); // 부분
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 30000); // 전체

        // 최종 결제 상태는 CANCEL
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL);

        // 주문 상태 확인
        assertThat(order1.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED); // 부분 취소한 주문 > 전체취소시 REFUNDED로 덮어씌워짐
        assertThat(order2.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);  // 전체 취소 시 환불 처리

        // 취소 이력 저장 2회 (부분 1 + 전체 1)
        verify(paymentCancelHistoryRepository, times(3)).save(any(PaymentCancelHistory.class));
    }


    @Test
    @DisplayName("부분 취소 후 전체 취소가 남은 금액에 대해 정상 처리된다")
    void cancelPartial_thenCancelFullPayment_성공2() {
        // given
        String paymentKey = "payKey123";
        Long partCancelOrderId = 1L;
        Long remainingOrderId = 2L;
        String cancelReason = "사용자 요청";

        // 주문 정보
        Order order1 = Order.builder().currentStatus(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order1, "id", partCancelOrderId);
        Order order2 = Order.builder().currentStatus(OrderStatus.PENDING).build();
        ReflectionTestUtils.setField(order2, "id", remainingOrderId);

        // PaymentOrder (주문 1: 부분취소 대상, 주문 2: 남은 결제 대상)
        PaymentOrder po1 = mock(PaymentOrder.class);
        PaymentOrder po2 = mock(PaymentOrder.class);
        given(po1.getOrder()).willReturn(order1);
        given(po1.getPaymentStatus()).willReturn(PaymentStatus.COMPLETE);
        given(po1.getPaymentAmount()).willReturn(20000);

        given(po2.getOrder()).willReturn(order2);
        given(po2.getPaymentAmount()).willReturn(30000);

        // Payment 객체
        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2));
        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));

        // 부분 취소 이력 반환: 20000원만 취소된 상태
        PaymentCancelHistory history = mock(PaymentCancelHistory.class);
        given(history.getCancelAmount()).willReturn(20000);

        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)).willReturn(
            List.of(history));
        given(paymentOrderRepository.findAllByPayment_Id(payment.getId())).willReturn(
            List.of(po1, po2));

        // when
        // 1. 부분 취소
        paymentService.cancelPartialPayment(paymentKey, partCancelOrderId, cancelReason);

        // 2. 전체 취소 (남은 30000원 대상)
        paymentService.cancelFullPayment(paymentKey, cancelReason);

        // then
        // Toss cancel이 2번 호출되어야 함 (20000 + 30000)
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 20000);
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 30000);

        // 전체 결제 상태가 CANCEL로 변경됨
        verify(paymentRepository, atLeastOnce()).save(
            argThat(p -> p.getStatus() == PaymentStatus.CANCEL));

        // 모든 주문 상태가 처리됨
        verify(orderRepository, atLeastOnce()).save(order1);
        verify(orderRepository, atLeastOnce()).save(order2);

        // 취소 이력 저장: 부분 취소 1회 + 전체 취소 시 2건 → 총 3회 호출됨
        verify(paymentCancelHistoryRepository, times(3)).save(any(PaymentCancelHistory.class));

    }

//    @Test
//    @DisplayName("부분 취소 3회로 누적 금액이 전액이면 결제 상태가 CANCEL, 주문은 REFUNDED 처리된다")
//    void 부분취소_누적합이_전체금액과_같으면_CANCEL로_전이() {
//        // given
//        String paymentKey = "payKey123";
//        String cancelReason = "사용자 요청";
//
//        // 주문 3개
//        Order order1 = Order.builder().currentStatus(OrderStatus.PENDING).build();
//        Order order2 = Order.builder().currentStatus(OrderStatus.PENDING).build();
//        Order order3 = Order.builder().currentStatus(OrderStatus.PENDING).build();
//        ReflectionTestUtils.setField(order1, "id", 1L);
//        ReflectionTestUtils.setField(order2, "id", 2L);
//        ReflectionTestUtils.setField(order3, "id", 3L);
//
//        // Payment
//        Payment payment = Payment.builder()
//            .paymentKey(paymentKey)
//            .amount(50000)
//            .status(PaymentStatus.COMPLETE)
//            .method(PaymentMethod.CARD)
//            .currency("KRW")
//            .sellerId(99L)
//            .build();
//        ReflectionTestUtils.setField(payment, "id", 1L);
//
//        // PaymentOrder (팩토리 메서드 사용)
//        PaymentOrder po1 = PaymentOrder.of(payment, order1, 20000);
//        PaymentOrder po2 = PaymentOrder.of(payment, order2, 20000);
//        PaymentOrder po3 = PaymentOrder.of(payment, order3, 10000);
//        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2, po3));
//
//        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
//        given(paymentOrderRepository.findAllByPayment_Id(payment.getId()))
//            .willReturn(List.of(po1, po2, po3));
//
//        // 취소 이력 mock
//        PaymentCancelHistory h1 = mock(PaymentCancelHistory.class);
//        PaymentCancelHistory h2 = mock(PaymentCancelHistory.class);
//        PaymentCancelHistory h3 = mock(PaymentCancelHistory.class);
//        given(h1.getCancelAmount()).willReturn(20000);
//        given(h2.getCancelAmount()).willReturn(20000);
//        given(h3.getCancelAmount()).willReturn(10000);
//
//        // 취소 이력 설정
//        AtomicInteger count = new AtomicInteger(0);
//        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment))
//            .willAnswer(invocation -> {
//                int i = count.getAndIncrement();
//                return switch (i) {
//                    case 0 -> List.of();               // 1차 취소 시점
//                    case 1 -> List.of(h1);             // 2차
//                    case 2 -> List.of(h1, h2);         // 3차 (이때 4만)
//                    default -> List.of(h1, h2, h3);    // 3차 이후 (최종 5만)
//                };
//            });
//
//
//        // when
//        paymentService.cancelPartialPayment(paymentKey, 1L, cancelReason); // 2만
//        paymentService.cancelPartialPayment(paymentKey, 2L, cancelReason); // 2만
//        paymentService.cancelPartialPayment(paymentKey, 3L, cancelReason); // 1만
//
//        // then
//        verify(tossPaymentClient, times(2)).requestCancel(paymentKey, cancelReason, 20000);
//        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 10000);
//
//        // 최종 결제 상태는 CANCEL
//        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCEL);
//
//        // 모든 주문 상태는 REFUNDED
//        assertThat(order1.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);
//        assertThat(order2.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);
//        assertThat(order3.getCurrentStatus()).isEqualTo(OrderStatus.REFUNDED);
//
//        // 취소 이력은 총 3건 저장됨
//        verify(paymentCancelHistoryRepository, times(3)).save(any(PaymentCancelHistory.class));
//    }

    @DisplayName("결제 취소 이력을 조회하면 PaymentCancelHistoryResponse 리스트를 반환한다")
    @Test
    void getCancelHistory_성공() {
        // given
        String paymentKey = "payKey123";

        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000)
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);

        Order order = Order.builder().currentStatus(OrderStatus.CANCELLED).build();
        PaymentOrder paymentOrder = PaymentOrder.of(payment, order, 50000);

        PaymentCancelHistory h1 = PaymentCancelHistory.create(paymentOrder, 20000, "사용자 요청");
        PaymentCancelHistory h2 = PaymentCancelHistory.create(paymentOrder, 30000, "배송 지연");

        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)).willReturn(List.of(h1, h2));

        // when
        List<PaymentCancelHistoryResponse> result = paymentService.getCancelHistory(paymentKey);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).cancelAmount()).isEqualTo(20000);
        assertThat(result.get(0).cancelReason()).isEqualTo("사용자 요청");
        assertThat(result.get(1).cancelAmount()).isEqualTo(30000);
        assertThat(result.get(1).cancelReason()).isEqualTo("배송 지연");

        verify(paymentRepository).findByPaymentKey(paymentKey);
        verify(paymentCancelHistoryRepository).findByPaymentOrder_Payment(payment);
    }

    @DisplayName("판매자 ID와 날짜 범위로 결제 내역을 조회하면 PaymentLookupResponse 리스트를 반환한다")
    @Test
    void getPaymentsBySellerId_성공() {
        // given
        Long sellerId = 99L;
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 3);

        // 주문 엔티티
        Order order1 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order1, "id", 101L);

        Order order2 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order2, "id", 102L);

        // 결제 엔티티
        Payment payment = Payment.builder()
            .paymentKey("payKey123")
            .tossOrderId("tossOrder123")
            .amount(50000)
            .currency("KRW")
            .status(PaymentStatus.COMPLETE)
            .sellerId(sellerId)
            .build();
        ReflectionTestUtils.setField(payment, "id", 1L);
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.of(2025, 7, 2, 12, 0));

        // 결제-주문 매핑
        PaymentOrder po1 = PaymentOrder.of(payment, order1, 20000);
        PaymentOrder po2 = PaymentOrder.of(payment, order2, 30000);

        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2));

        given(paymentRepository.findBySellerIdAndCreatedAtBetween(
            eq(sellerId),
            eq(startDate.atStartOfDay()),
            eq(endDate.plusDays(1).atStartOfDay())
        )).willReturn(List.of(payment));

        // when
        List<PaymentLookupResponse> result = paymentService.getPaymentsBySellerId(sellerId, startDate, endDate);

        // then
        assertThat(result).hasSize(1);
        PaymentLookupResponse response = result.get(0);
        assertThat(response.tossOrderId()).isEqualTo("tossOrder123");
        assertThat(response.paymentKey()).isEqualTo("payKey123");

        // orders 안의 OrderPaymentResponse 리스트의 값들을 직접 비교
        List<OrderPaymentResponse> orders = response.orders();
        assertThat(orders).extracting("orderId").containsExactlyInAnyOrder(101L, 102L);
        assertThat(orders).extracting("orderAmount").containsExactlyInAnyOrder(20000, 30000);

        verify(paymentRepository).findBySellerIdAndCreatedAtBetween(
            eq(sellerId), eq(startDate.atStartOfDay()), eq(endDate.plusDays(1).atStartOfDay())
        );
    }

    @DisplayName("사용자 ID와 날짜 범위로 결제 내역을 조회하면 UserPaymentLookupResponse 리스트를 반환한다")
    @Test
    void getPaymentsByUser_성공() {
        // given
        Long userId = 77L;
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 7, 5);

        Order order1 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order1, "id", 201L);

        Order order2 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order2, "id", 202L);

        Payment payment = Payment.builder()
            .paymentKey("payKey-user")
            .tossOrderId("tossOrder-user")
            .amount(40000)
            .currency("KRW")
            .status(org.example.oshipserver.domain.payment.entity.PaymentStatus.COMPLETE)
            .sellerId(userId)
            .build();
        ReflectionTestUtils.setField(payment, "id", 10L);
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.of(2025, 7, 3, 10, 0));
        ReflectionTestUtils.setField(payment, "paidAt", LocalDateTime.of(2025, 7, 3, 11, 0));

        PaymentOrder po1 = PaymentOrder.of(payment, order1, 15000);
        PaymentOrder po2 = PaymentOrder.of(payment, order2, 25000);
        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2));

        given(paymentRepository.findBySellerIdAndCreatedAtBetween(
            eq(userId),
            eq(startDate.atStartOfDay()),
            eq(endDate.plusDays(1).atStartOfDay())
        )).willReturn(List.of(payment));

        // when
        List<UserPaymentLookupResponse> result = paymentService.getPaymentsByUser(userId, startDate, endDate);

        // then
        assertThat(result).hasSize(1);
        UserPaymentLookupResponse response = result.get(0);
        assertThat(response.amount()).isEqualTo(40000);
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.paymentStatus()).isEqualTo("COMPLETE");
        assertThat(response.paidAt()).isEqualTo("2025-07-03T11:00");

        List<OrderPaymentResponse> orders = response.orders();
        assertThat(orders).extracting("orderId").containsExactlyInAnyOrder(201L, 202L);
        assertThat(orders).extracting("orderAmount").containsExactlyInAnyOrder(15000, 25000);

        verify(paymentRepository).findBySellerIdAndCreatedAtBetween(
            eq(userId), eq(startDate.atStartOfDay()), eq(endDate.plusDays(1).atStartOfDay())
        );
    }

    @Test
    @DisplayName("부분 취소 요청 시 해당 주문의 orderStatus는 CANCELLED, 나머지 주문은 PAID 상태가 유지된다")
    void cancelPartialPayment_부분취소시_주문상태_검증() {
        // given
        String paymentKey = "payKey-partial-test";
        Long targetOrderId = 1L;
        Long otherOrderId = 2L;
        String cancelReason = "부분 취소 테스트";

        // 주문 객체
        Order order1 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order1, "id", targetOrderId);

        Order order2 = Order.builder().currentStatus(OrderStatus.PAID).build();
        ReflectionTestUtils.setField(order2, "id", otherOrderId);

        // 결제 객체
        Payment payment = Payment.builder()
            .paymentKey(paymentKey)
            .amount(50000) // 전체 결제 금액
            .status(PaymentStatus.COMPLETE)
            .build();
        ReflectionTestUtils.setField(payment, "id", 10L);

        // 실제 PaymentOrder 객체 생성
        PaymentOrder po1 = PaymentOrder.of(payment, order1, 20000);
        PaymentOrder po2 = PaymentOrder.of(payment, order2, 30000);
        ReflectionTestUtils.setField(payment, "paymentOrders", List.of(po1, po2));

        // 취소 이력 없음 → 누적 취소 금액 0
        given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
        given(paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)).willReturn(List.of());

        // when
        paymentService.cancelPartialPayment(paymentKey, targetOrderId, cancelReason);

        // then
        verify(tossPaymentClient).requestCancel(paymentKey, cancelReason, 20000);
        verify(paymentOrderRepository).save(po1);
        verify(paymentCancelHistoryRepository).save(any(PaymentCancelHistory.class));

        // 상태 검증
        assertThat(order1.getCurrentStatus()).isEqualTo(OrderStatus.CANCELLED); // 실제 코드가 PARTIAL_CANCEL로 변경되면 여기만 바꾸면 됨
        assertThat(order2.getCurrentStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIAL_CANCEL);
    }
}