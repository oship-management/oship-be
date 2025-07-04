package org.example.oshipserver.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.font.TextHitInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
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
import org.example.oshipserver.domain.payment.dto.response.PaymentOrderListResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossSinglePaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.UserPaymentLookupResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentCancelHistory;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentMethodMapper;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;
import org.example.oshipserver.domain.payment.repository.PaymentCancelHistoryRepository;
import org.example.oshipserver.domain.payment.repository.PaymentOrderRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.domain.payment.util.PaymentNoGenerator;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.example.oshipserver.global.exception.ApiException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.transaction.annotation.Transactional;
import org.example.oshipserver.domain.payment.dto.response.PaymentCancelHistoryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IdempotentRestClient idempotentRestClient;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCancelHistoryRepository paymentCancelHistoryRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final PaymentNotificationService paymentNotificationService;

    /**
     * 단건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
     */
    @Transactional(rollbackFor = ApiException.class)
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {

        // 1. DB 기준 중복 확인 (동시성 보장x)
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
        }

        // 2. 오늘 날짜 기준 생성된 결제 수 조회하여 시퀀스 결정 >> paymentNo생성 (멱등성키로 활용)
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 3. Toss API 호출
        TossPaymentConfirmResponse tossResponse;
        try {
            Map<String, Object> tossRequestBody = Map.of(
                "paymentKey", request.paymentKey(),
                "orderId", request.tossOrderId(),
                "amount", request.amount(),
                "currency", "KRW"
            );

            // PaymentService 내에서 tossPaymentClient 대신 idempotentRestClient 호출
            tossResponse = idempotentRestClient.postForIdempotent(
                "https://api.tosspayments.com/v1/payments/confirm",
                tossRequestBody,
                TossPaymentConfirmResponse.class,
                paymentNo
            );

            try {
                log.info("[Toss 응답 JSON] {}", objectMapper.writeValueAsString(tossResponse));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("Toss 응답 JSON 직렬화 실패", e);
            }

        } catch (ApiException e) {
            log.warn("Toss 결제 승인 실패. paymentNo={}, reason={}", paymentNo, e.getMessage());
            throw e;
        }

        Order order = orderRepository.findById(request.orderId())
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 4. 결제 방식 매핑
        PaymentMethod method = PaymentMethodMapper.fromToss(tossResponse);

        // 5. Toss 응답값을 Payment 엔티티로 변환하여 저장
        Payment payment = Payment.builder()
            .idempotencyKey(paymentNo)
            .paymentNo(paymentNo)
            .tossOrderId(tossResponse.getOrderId())
            .paymentKey(tossResponse.getPaymentKey())
            .amount(tossResponse.getTotalAmount())
            .currency(tossResponse.getCurrency())
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.getApprovedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.getStatus()))
            .sellerId(order.getSellerId())
            .build();

        if (tossResponse.getCard() != null) {
            payment.setCardLast4Digits(getLast4Digits(tossResponse.getCard().getNumber()));
        }
        payment.setReceiptUrl(tossResponse.getReceipt().getUrl());

        // 6. 결제 저장
        paymentRepository.save(payment);

        // 7. 결제-주문 중간 테이블 저장
        PaymentOrder paymentOrder = PaymentOrder.builder()
            .payment(payment)
            .order(order)
            .paymentAmount(tossResponse.getTotalAmount())
            .paymentStatus(payment.getStatus())
            .confirmedAt(payment.getPaidAt())
            .build();

        paymentOrderRepository.save(paymentOrder);

        // 8. 주문 상태 업데이트
        try {
            if (!order.getCurrentStatus().equals(OrderStatus.PAID)) {
                order.markAs(OrderStatus.PAID);
                orderRepository.save(order);
                log.info("주문 상태가 PAID로 변경되었습니다. orderId={}", order.getId());
            }
        } catch (IllegalStateException e) {
            log.warn("주문 상태 변경 실패: orderId={}, currentStatus={}, targetStatus=PAID, reason={}",
                order.getId(), order.getCurrentStatus(), e.getMessage());
        }

        // 9. 큐 기반 비동기 이메일 전송 (결제 완료 알림)
        paymentNotificationService.sendPaymentCompletedV2(payment);

        // 10. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse, payment.getMethod());
    }

    /**
     * 다건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
     */
    @Transactional(rollbackFor = ApiException.class)
    public MultiPaymentConfirmResponse confirmMultiPayment(MultiPaymentConfirmRequest request) {
        // 1. 중복 결제 방지 (paymentKey)
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
        }

        // 2. 오늘 날짜 기준으로 생성된 결제 건 수 조회 >> 고유 paymentNo 생성
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(), today.plusDays(1).atStartOfDay()
        );
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 3. Toss 결제 승인 api 호출
        TossPaymentConfirmResponse tossResponse;
        try {
            Map<String, Object> tossRequestBody = Map.of(
                "paymentKey", request.paymentKey(),
                "orderId", request.tossOrderId(),
                "amount", request.orders().stream().mapToInt(MultiOrderRequest::amount).sum(),
                "currency", "KRW"
            );

            // PaymentService 내에서 tossPaymentClient 대신 idempotentRestClient 호출
            tossResponse = idempotentRestClient.postForIdempotent(
                "https://api.tosspayments.com/v1/payments/confirm",
                tossRequestBody,
                TossPaymentConfirmResponse.class,
                paymentNo
            );

            try {
                log.info("[Toss 응답 JSON] {}", objectMapper.writeValueAsString(tossResponse));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("Toss 응답 JSON 직렬화 실패", e);
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
            }
            throw e;
        }

        // 4. 대표 주문으로부터 sellerId 추출
        Long representativeOrderId = request.orders().get(0).orderId();
        Order mainOrder = orderRepository.findById(representativeOrderId)
            .orElseThrow(() -> new ApiException("대표 주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 5. 결제 방식 매핑
        PaymentMethod method = PaymentMethodMapper.fromToss(tossResponse);

        // 6. toss 응답 기반으로 payment 엔티티 생성 및 저장
        Payment payment = Payment.builder()
            .idempotencyKey(paymentNo)
            .paymentNo(paymentNo)
            .paymentKey(tossResponse.getPaymentKey())
            .tossOrderId(tossResponse.getOrderId()) // Toss의 orderId 저장
            .amount(tossResponse.getTotalAmount())
            .currency("KRW")
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.getApprovedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.getStatus()))
            .sellerId(mainOrder.getSellerId())
            .build();

        if (tossResponse.getCard() != null) {
            payment.setCardLast4Digits(getLast4Digits(tossResponse.getCard().getNumber()));
        }
        payment.setReceiptUrl(tossResponse.getReceipt().getUrl());

        paymentRepository.save(payment);

        // 7. PaymentOrder 생성 (주문 리스트 하나씩 매핑)
        for (MultiOrderRequest o : request.orders()) {
            Order order = orderRepository.findById(o.orderId())
                .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

            // 결제-주문 연결하여 저장
            PaymentOrder paymentOrder = PaymentOrder.builder()
                .payment(payment)
                .order(order)
                .paymentAmount(o.amount())
                .paymentStatus(payment.getStatus())
                .confirmedAt(payment.getPaidAt())
                .build();
            paymentOrderRepository.save(paymentOrder);

            // 주문 상태 업데이트
            if (!order.getCurrentStatus().equals(OrderStatus.PAID)) {
                try {
                    order.markAs(OrderStatus.PAID);
                    orderRepository.save(order);
                    log.info("주문 상태가 PAID로 변경되었습니다. orderId={}", order.getId());
                } catch (IllegalStateException e) {
                    log.warn("주문 상태를 PAID로 변경하지 못했습니다. orderId={}, currentStatus={}, reason={}",
                        order.getId(), order.getCurrentStatus(), e.getMessage());
                }
            }
        }

        // 8. 큐 기반 비동기 이메일 전송 (결제 완료 알림)
        paymentNotificationService.sendPaymentCompletedV2(payment);

        // 9. 응답용 orderId 리스트 추출
        List<String> orderIds = request.orders().stream()
            .map(o -> o.orderId().toString())
            .toList();

        return MultiPaymentConfirmResponse.convertFromTossConfirm(tossResponse, orderIds);
    }

    /**
     * Toss 전체 취소 요청
     * @param paymentKey
     * @param cancelReason
     */
    @Transactional
    public void cancelFullPayment(String paymentKey, String cancelReason) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new ApiException("결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 2. 기존 취소 이력 합산
        int totalCanceledAmount = paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)
            .stream()
            .mapToInt(PaymentCancelHistory::getCancelAmount)
            .sum();

        int remainingAmount = payment.getAmount() - totalCanceledAmount;

        if (remainingAmount <= 0) {
            throw new ApiException("이미 전체 금액이 취소되었습니다.", ErrorType.ALREADY_CANCELED);
        }

        // 3. 남은 금액만큼, Toss에 전체취소 요청
        tossPaymentClient.requestCancel(paymentKey, cancelReason, remainingAmount);

        // 4. paymentStatus 변경
        try {
            payment.updateStatus(PaymentStatus.CANCEL);
            paymentRepository.save(payment);
            log.info("결제 상태를 CANCEL로 변경했습니다. paymentNo={}", payment.getPaymentNo());
        } catch (IllegalStateException ex) {
            log.warn("결제 상태 CANCEL로 변경 실패: 현재 상태={}, paymentNo={}, reason={}",
                payment.getStatus(), payment.getPaymentNo(), ex.getMessage());
        }

        // 5. PaymentOrder + Order 상태도 전체 취소로 변경
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment_Id(payment.getId());

        for (PaymentOrder po : paymentOrders) {
            po.cancel(); // paymentStatus : CANCEL
            paymentOrderRepository.save(po);

            Order order = po.getOrder();
            try {
                order.markAs(OrderStatus.REFUNDED); // 주문상태 변경
                orderRepository.save(order);
                log.info("주문 상태가 REFUNDED로 변경되었습니다. orderId={}", order.getId());
            } catch (IllegalStateException e) {
                log.warn("주문 상태를 REFUNDED로 변경하지 못했습니다. orderId={}, currentStatus={}, reason={}",
                    order.getId(), order.getCurrentStatus(), e.getMessage());
            }
        }

        // 6. 취소 이력 저장
        for (PaymentOrder po : paymentOrders) {
            PaymentCancelHistory history = PaymentCancelHistory.create(po, po.getPaymentAmount(), cancelReason);
            paymentCancelHistoryRepository.save(history);
        }

        // 7. 큐 기반 비동기 이메일 전송 (결제 취소 알림)
        paymentNotificationService.sendPaymentCancelledV2(payment);
    }

    /**
     * Toss 부분 취소 요청
     * @param paymentKey
     * @param orderId
     * @param cancelReason
     */
    @Transactional
    public void cancelPartialPayment(String paymentKey, Long orderId, String cancelReason) {
        // 1. 결제 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new ApiException("결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 2. 해당 주문이 결제에 포함되어 있는지 확인 (orderId 기준으로만 취소 가능하도록)
        PaymentOrder paymentOrder = payment.getPaymentOrders().stream()
            .filter(po -> po.getOrder().getId().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new ApiException("해당 주문은 이 결제에 포함되어 있지 않습니다.", ErrorType.INVALID_ORDER));

        // 3. 이미 취소된 경우 방지
        if (paymentOrder.getPaymentStatus() == PaymentStatus.CANCEL) {
            throw new ApiException("이미 취소된 주문입니다.", ErrorType.ALREADY_CANCELED);
        }

        // 부분취소금액을 외부에서 입력받지 않고, 내부에서 order 금액 가져오는 방식으로 리팩토링
        // 결제 금액 계산 : 취소금액 = order 단위의 전체 금액
        int cancelAmount = paymentOrder.getPaymentAmount();

        // 5. Toss에 부분취소 요청
        tossPaymentClient.requestCancel(paymentKey, cancelReason, cancelAmount);

        // 6. 결제 금액 유효성 체크 (상태 변경은 나중에)
        try {
            payment.partialCancel(cancelAmount);
        } catch (IllegalArgumentException e) {
            throw new ApiException("부분취소 금액이 유효하지 않습니다.", ErrorType.PAYMENT_INVALID_CANCEL_AMOUNT);
        }

        // 7. PaymentOrder 상태만 CANCEL 처리
        paymentOrder.cancel();  // paymentStatus : CANCEL
        paymentOrderRepository.save(paymentOrder);

        // 취소된 order의 상태만 PARTIAL_CANCEL로 변경
        Order order = paymentOrder.getOrder();
        try {
            order.markAs(OrderStatus.CANCELLED);
            log.info("주문 상태가 CANCELLED로 변경되었습니다. orderId={}", order.getId());
            orderRepository.save(order);
        } catch (IllegalStateException ex) {
            log.warn("주문 상태를 CANCELLED로 변경하지 못했습니다. orderId={}, currentStatus={}, reason={}",
                order.getId(), order.getCurrentStatus(), ex.getMessage());
        }

        // 8. 취소 이력 저장
        PaymentCancelHistory history = PaymentCancelHistory.create(paymentOrder, cancelAmount, cancelReason);
        paymentCancelHistoryRepository.save(history);

        // 9. 누적 취소 금액 계산
        int totalCanceledAmount = paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment)
            .stream()
            .mapToInt(PaymentCancelHistory::getCancelAmount)
            .sum();

        // 10. 전체 취소 여부 체크
        if (totalCanceledAmount == payment.getAmount()) {
            try {
                payment.updateStatus(PaymentStatus.CANCEL);
                paymentRepository.save(payment);
                log.info("결제 상태가 CANCEL로 변경되었습니다. paymentNo={}", payment.getPaymentNo());
            } catch (IllegalStateException ex) {
                log.warn("결제 상태를 CANCEL로 변경하지 못했습니다. paymentNo={}, currentStatus={}, reason={}",
                    payment.getPaymentNo(), payment.getStatus(), ex.getMessage());
            }

            // 모든 주문 상태를 REFUNDED로 변경
            for (PaymentOrder po : payment.getPaymentOrders()) {
                Order o = po.getOrder();
                try {
                    o.markAs(OrderStatus.REFUNDED);
                    log.info("주문 상태가 REFUNDED로 변경되었습니다. orderId={}", order.getId());
                    orderRepository.save(o);
                } catch (IllegalStateException exx) {
                    log.warn("주문 상태 REFUNDED 전이 실패. orderId={}, currentStatus={}, reason={}",
                        o.getId(), o.getCurrentStatus(), exx.getMessage());
                }
            }
            // 큐 기반 비동기 이메일 전송 (전체 취소가 완료된 시점에 결제 취소 알림)
            paymentNotificationService.sendPaymentCancelledV2(payment);
        } else {
            // 전체 취소가 아닌 경우에만 PARTIAL_CANCEL 적용
            try {
                payment.updateStatus(PaymentStatus.PARTIAL_CANCEL);
                paymentRepository.save(payment);
                log.info("결제 상태가 PARTIAL_CANCEL로 변경되었습니다. paymentNo={}", payment.getPaymentNo());
            } catch (IllegalStateException e) {
                log.warn("결제 상태 PARTIAL_CANCEL로 변경 실패. 현재 상태={}, paymentNo={}, reason={}",
                    payment.getStatus(), payment.getPaymentNo(), e.getMessage());
                throw new ApiException("PARTIAL_CANCEL 상태로 변경할 수 없습니다.", ErrorType.PAYMENT_STATUS_TRANSITION_FAILED
                );
            }
        }
        //
    }

    /**
     * 결제 취소 이력 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentCancelHistoryResponse> getCancelHistory(String paymentKey) {
        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new ApiException("결제 정보가 없습니다.", ErrorType.NOT_FOUND));

        // 2. PaymentOrder 기준으로 취소이력 조회
        List<PaymentCancelHistory> histories = paymentCancelHistoryRepository.findByPaymentOrder_Payment(payment);

        // 3. DTO 변환
        return histories.stream()
            .map(PaymentCancelHistoryResponse::fromEntity)
            .toList();
    }

    /**
     * sellerId를 기준으로 결제 요청 내역 조회
     * 관리자 페이지용 (tossOrderId, paymentKey 조회됨)
     */
    @Transactional(readOnly = true)
    public List<PaymentLookupResponse> getPaymentsBySellerId(Long sellerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : LocalDate.MAX.atStartOfDay();

        List<Payment> payments = paymentRepository.findBySellerIdAndCreatedAtBetween(sellerId, start, end);

        return payments.stream()
            .map(payment -> {
                List<Order> orders = payment.getOrders();

                Map<Long, Integer> orderAmounts = payment.getPaymentOrders().stream()
                    .collect(Collectors.toMap(
                        po -> po.getOrder().getId(),
                        PaymentOrder::getPaymentAmount
                    ));

                return PaymentLookupResponse.fromPaymentEntity(payment, orders, orderAmounts);
            })
            .toList();
    }

    /**
     * 사용자 본인의 결제 내역 조회
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public List<UserPaymentLookupResponse> getPaymentsByUser(Long userId, LocalDate startDate, LocalDate endDate) {
        // 날짜 기본값 설정
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime end = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : LocalDate.MAX.atStartOfDay();

        // 날짜로 해당 사용자 결제 목록 조회
        List<Payment> payments = paymentRepository.findBySellerIdAndCreatedAtBetween(userId, start, end);

        return payments.stream()
            .map(payment -> {
                List<Order> orders = payment.getOrders(); // 해당 결제에 연결된 모든 주문리스트 가져옴

                // Map 생성; 주문별 결제 금액 추출
                Map<Long, Integer> orderAmounts = payment.getPaymentOrders().stream()
                    .collect(Collectors.toMap(
                        po -> po.getOrder().getId(),
                        PaymentOrder::getPaymentAmount
                    ));

                List<OrderPaymentResponse> orderResponses = orders.stream()
                    .map(order -> OrderPaymentResponse.from(order, orderAmounts.get(order.getId())))
                    .toList();

                return UserPaymentLookupResponse.fromPaymentEntityForUser(payment, orderResponses);
            })
            .toList();
    }

    private String getLast4Digits(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return cardNumber.substring(cardNumber.length() - 4);
        }
        return null;
    }

}