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
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.example.oshipserver.global.exception.ApiException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.transaction.annotation.Transactional;
import org.example.oshipserver.domain.payment.dto.response.PaymentCancelHistoryResponse;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCancelHistoryRepository paymentCancelHistoryRepository;
    private final OrderRepository orderRepository;

    /**
     * 단건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
     */
    @Transactional
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
            tossResponse = tossPaymentClient.requestPaymentConfirm(
                new PaymentConfirmRequest(
                    request.paymentKey(),
                    null,  // 서버 orderId는 Toss에 전달하지 않음
                    request.tossOrderId(),
                    request.amount()
                ),
                paymentNo
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
            }
            throw e;
        }

        Order order = orderRepository.findById(request.orderId())
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 4. 결제 방식 매핑
        PaymentMethod method = PaymentMethodMapper.fromToss(tossResponse);

        // 5. Toss 응답값을 Payment 엔티티로 변환하여 저장
        Payment payment = Payment.builder()
            .paymentNo(paymentNo)
            .tossOrderId(tossResponse.orderId())
            .paymentKey(tossResponse.paymentKey())
            .amount(tossResponse.totalAmount())
            .currency(tossResponse.currency())
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .sellerId(order.getSellerId())
            .build();

        if (tossResponse.card() != null) {
            payment.setCardLast4Digits(getLast4Digits(tossResponse.card().number()));
        }
        payment.setReceiptUrl(tossResponse.receipt().url());

        // 6. 결제 저장
        paymentRepository.save(payment);

        // 7. 결제-주문 중간 테이블 저장
        PaymentOrder paymentOrder = PaymentOrder.builder()
            .payment(payment)
            .order(order)
            .paymentAmount(tossResponse.totalAmount())
            .paymentStatus(payment.getStatus())
            .confirmedAt(payment.getPaidAt())
            .build();

        paymentOrderRepository.save(paymentOrder);

        // 8. 주문 상태 업데이트
        if (!order.getCurrentStatus().equals(OrderStatus.PAID)) {  // 중복 상태 변경 방지
            order.markAsPaid();
        }
        orderRepository.save(order);

        // 9. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse, payment.getMethod());
    }

    /**
     * 다건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
     */
    @Transactional
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
            tossResponse = tossPaymentClient.requestPaymentConfirm(
                new PaymentConfirmRequest(
                    request.paymentKey(),
                    null,  // Toss에 서버 orderId 넘기지 않음
                    request.tossOrderId(),
                    request.orders().stream()
                        .mapToInt(MultiOrderRequest::amount)
                        .sum()
                ),
                paymentNo
            );
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
            .paymentNo(paymentNo)
            .paymentKey(tossResponse.paymentKey())
            .tossOrderId(tossResponse.orderId())  // Toss의 orderId 저장
            .amount(tossResponse.totalAmount())
            .currency("KRW")
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .sellerId(mainOrder.getSellerId())
            .build();

        if (tossResponse.card() != null) {
            payment.setCardLast4Digits(getLast4Digits(tossResponse.card().number()));
        }
        payment.setReceiptUrl(tossResponse.receipt().url());

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
            if (!order.getCurrentStatus().equals(OrderStatus.PAID)) {  // 중복 상태 변경 방지
                order.markAsPaid();
            }
            orderRepository.save(order);
        }

        // 8. 응답용 orderId 리스트 추출
        List<String> orderIds = request.orders().stream()
            .map(o -> o.orderId().toString())
            .toList();

        return MultiPaymentConfirmResponse.convertFromTossConfirm(tossResponse, orderIds);
    }

//    /**
//     * Toss 기준 결제 조회 (결제상태 확인용)
//     * tossOrderId로 단건 조회 또는 다건 조회(대표 orderId)
//     */
//    @Transactional(readOnly = true)
//    public PaymentLookupResponse getPaymentByTossOrderId(String tossOrderId) {
//        Payment payment = paymentRepository.findByTossOrderId(tossOrderId)
//            .orElseThrow(() -> new ApiException("해당 주문의 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
//
//        List<Order> orders = payment.getOrders();  // 연결된 주문 목록 조회
//
//        return PaymentLookupResponse.fromPaymentAndOrders(payment, orders);
//    }
//
//    /**
//     * Toss 기준 결제 조회 (주문 확인용)
//     * -> 해당 payment에 연결된 모든 order를 주문리스트로 반환
//     */
//    @Transactional(readOnly = true)
//    public List<PaymentOrderListResponse> getOrdersByTossOrderId(String tossOrderId) {
//        // 결제 정보 조회
//        Payment payment = paymentRepository.findByTossOrderId(tossOrderId)
//            .orElseThrow(() -> new ApiException("해당 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
//
//        // 2. 결제에 연결된 모든 주문 조회
//        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment_Id(payment.getId());
//
//        if (paymentOrders.isEmpty()) {
//            throw new ApiException("해당 결제에 연결된 주문이 없습니다.", ErrorType.NOT_FOUND);
//        }
//
//        // 3. 주문 리스트를 DTO로 변환
//        return paymentOrders.stream()
//            .map(po -> PaymentOrderListResponse.from(po.getOrder()))
//            .toList();
//    }

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
        int totalCanceledAmount = paymentCancelHistoryRepository.findByPayment(payment)
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
        payment.cancel();
        paymentRepository.save(payment);

        // 5. PaymentOrder + Order 상태도 전체 취소로 변경
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment_Id(payment.getId());
        for (PaymentOrder po : paymentOrders) {
            po.cancel();
            po.getOrder().markAsCancelled(); // orderStatus 변경
            orderRepository.save(po.getOrder());
        }

        // 6. 취소 이력 저장
        PaymentCancelHistory history = PaymentCancelHistory.create(payment, remainingAmount, cancelReason, null);
        paymentCancelHistoryRepository.save(history);
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
        // 취소금액 = order 단위의 전체 금액
        int cancelAmount = paymentOrder.getPaymentAmount();

        // 5. Toss에 부분취소 요청
        tossPaymentClient.requestCancel(paymentKey, cancelReason, cancelAmount);

        // 6. 결제 상태 변경
        payment.partialCancel(cancelAmount, cancelReason);
        paymentRepository.save(payment);

        // 7. 주문 상태 변경
        paymentOrder.cancel();
        paymentOrder.getOrder().markAsCancelled(); // order상태를 CANCELLED로
        paymentOrderRepository.save(paymentOrder);
        orderRepository.save(paymentOrder.getOrder());

        // 8. 취소 이력 저장
        PaymentCancelHistory history = PaymentCancelHistory.create(payment, cancelAmount, cancelReason, paymentOrder.getOrder());
        paymentCancelHistoryRepository.save(history);

        // 9. 누적 취소 금액 계산
        int totalCanceledAmount = paymentCancelHistoryRepository.findByPayment(payment)
            .stream()
            .mapToInt(PaymentCancelHistory::getCancelAmount)
            .sum();

        // 10. 전체 취소 여부 체크
        if (totalCanceledAmount == payment.getAmount()) { // 누적취소금액과 결제금액이 같을 경우
            payment.cancel(); // paymentStatus CANCEL로 전환
            paymentRepository.save(payment);

            payment.getPaymentOrders().forEach(po -> {
                po.getOrder().markAsRefunded(); // orderStatus REFUNDED로 전환
                orderRepository.save(po.getOrder());
            });
        }
    }

    /**
     * 결제 취소 이력 조회
     */
    @Transactional(readOnly = true)
    public List<PaymentCancelHistoryResponse> getCancelHistory(String paymentKey) {
        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new ApiException("결제 정보가 없습니다.", ErrorType.NOT_FOUND));

        // 2. 취소이력 조회
        List<PaymentCancelHistory> histories = paymentCancelHistoryRepository.findByPayment(payment);

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