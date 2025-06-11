package org.example.oshipserver.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.font.TextHitInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import org.example.oshipserver.client.toss.TossPaymentClient;
import org.example.oshipserver.domain.payment.dto.request.MultiPaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.request.PaymentConfirmRequest;
import org.example.oshipserver.domain.payment.dto.response.MultiPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentLookupResponse;
import org.example.oshipserver.domain.payment.dto.response.PaymentOrderListResponse;
import org.example.oshipserver.domain.payment.dto.response.TossPaymentConfirmResponse;
import org.example.oshipserver.domain.payment.dto.response.TossSinglePaymentLookupResponse;
import org.example.oshipserver.domain.payment.entity.Payment;
import org.example.oshipserver.domain.payment.entity.PaymentMethod;
import org.example.oshipserver.domain.payment.entity.PaymentOrder;
import org.example.oshipserver.domain.payment.entity.PaymentStatus;
import org.example.oshipserver.domain.payment.mapper.PaymentMethodMapper;
import org.example.oshipserver.domain.payment.mapper.PaymentStatusMapper;
import org.example.oshipserver.domain.payment.repository.PaymentOrderRepository;
import org.example.oshipserver.domain.payment.repository.PaymentRepository;
import org.example.oshipserver.domain.payment.util.PaymentNoGenerator;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.example.oshipserver.global.exception.ApiException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final PaymentRepository paymentRepository;
    private final PaymentOrderRepository paymentOrderRepository;


    // 단건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {

        // 1. DB 기준 중복 확인
        if (paymentRepository.existsByPaymentKey(request.paymentKey())) {
            throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
        }

        // 2. 오늘 날짜 기준 생성된 결제 수 조회하여 시퀀스 결정 (paymnentNo 생성용)
        LocalDate today = LocalDate.now();
        int todayCount = paymentRepository.countByCreatedAtBetween(
            today.atStartOfDay(),
            today.plusDays(1).atStartOfDay()
        );

        // 3. 고유 paymentNo 생성 >> 멱등성 키로 활용
        String paymentNo = PaymentNoGenerator.generate(today, todayCount + 1);

        // 4. RestTemplate를 사용하여 Toss 결제 승인 API 호출
        // Toss 응답 기준, 이미 처리된 요청에 대하여 409 에러
        TossPaymentConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.requestPaymentConfirm(request, paymentNo);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
            }
            throw e;
        }

        // 5. 실제 결제 방식 추후 매핑 예정
        PaymentMethod method = PaymentMethod.CARD;
        // PaymentMethod method = PaymentMethodMapper.fromToss(tossResponse);

        // 6. Toss 응답값을 Payment 엔티티로 변환하여 저장
        Payment payment = Payment.builder()
            .paymentNo(paymentNo)
            .tossOrderId(request.orderId())
            .paymentKey(tossResponse.paymentKey())
            .amount(tossResponse.totalAmount())
            .currency("KRW")
            .method(method)
            .paidAt(OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .build();

        paymentRepository.save(payment);

        // 7. 응답 DTO 반환
        return PaymentConfirmResponse.convertFromTossConfirm(tossResponse, method);
    }

    // 다건 결제 승인 요청 (Toss 결제 위젯을 통한 요청 처리)
    @Transactional
    public MultiPaymentConfirmResponse confirmMultiPayment(MultiPaymentConfirmRequest request) {
        // 1. 이미 동일한 paymentKey로 결제가 처리된 경우 예외 발생
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
                    request.orders().get(0).orderId(),
                    request.orders().stream().mapToInt(MultiPaymentConfirmRequest.MultiOrderRequest::amount).sum()
                ),
                paymentNo
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ApiException("이미 처리된 결제입니다.", ErrorType.DUPLICATED_PAYMENT);
            }
            throw e;
        }

        // 4. toss 응답 기반으로 payment 엔티티 생성 및 저장
        Payment payment = Payment.builder()
            .paymentNo(paymentNo)
            .paymentKey(tossResponse.paymentKey())
            .tossOrderId(tossResponse.orderId())  // Toss에서 내려준 orderId 사용
            .amount(tossResponse.totalAmount())
            .currency("KRW")
            .method(PaymentMethod.CARD)
            .paidAt(OffsetDateTime.parse(tossResponse.approvedAt()).toLocalDateTime())
            .status(PaymentStatusMapper.fromToss(tossResponse.status()))
            .build();

        paymentRepository.save(payment);

        // 5. 요청으로 들어온 각 주문의 orderId만 리스트로 추출하여 응답dto로 변환
        List<String> orderIds = request.orders().stream()
            .map(MultiPaymentConfirmRequest.MultiOrderRequest::orderId)
            .toList();

        return MultiPaymentConfirmResponse.convertFromTossConfirm(tossResponse, orderIds);
    }


    // tossOrderId로 결제 조회(toss 기준의 결제 단위 조회)
    // 단건 조회 또는 다건(대표 orderId) 조회
    @Transactional(readOnly = true)
    public PaymentLookupResponse getPaymentByTossOrderId(String tossOrderId) {
        Payment payment = paymentRepository.findByTossOrderId(tossOrderId)
            .orElseThrow(() -> new ApiException("해당 주문의 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        // 클라이언트에게 받아온 orderId를 통해 db에서 paymentKey를 꺼내서 toss API에 넘기기
        TossSinglePaymentLookupResponse tossResponse =
            tossPaymentClient.requestSinglePaymentLookup(payment.getPaymentKey());

        return PaymentLookupResponse.convertFromTossLookup(tossResponse);
    }

    // tossOrderId로 다건 결제 조회
    // tossOrderId 기준으로 결제 정보를 가져와서 해당 결제에 연결된 모든 주문를 리스트로 반환
    @Transactional(readOnly = true)
    public List<PaymentOrderListResponse> getOrdersByTossOrderId(String tossOrderId) {
        Payment payment = paymentRepository.findByTossOrderId(tossOrderId)
            .orElseThrow(() -> new ApiException("해당 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByPayment(payment);

        return paymentOrders.stream()
            .map(po -> PaymentOrderListResponse.from(po.getOrder()))
            .toList();
    }


    // 내부 orderId(Long) 기준으로 해당 주문에 연결된 결제 조회
    @Transactional(readOnly = true)
    public PaymentLookupResponse getPaymentByOrderId(Long orderId) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrder_Id(orderId)
            .orElseThrow(() -> new ApiException("해당 주문의 결제 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        Payment payment = paymentOrder.getPayment();

        TossSinglePaymentLookupResponse tossResponse =
            tossPaymentClient.requestSinglePaymentLookup(payment.getPaymentKey());

        return PaymentLookupResponse.convertFromTossLookup(tossResponse);
    }

    // 하나의 orderId에 연결된 모든 결제 조회 (확장용)
    @Transactional(readOnly = true)
    public List<PaymentLookupResponse> getAllPaymentsByOrderId(Long orderId) {
        // 1. 해당 주문에 연결된 모든 PaymentOrder 조회
        List<PaymentOrder> paymentOrders = paymentOrderRepository.findAllByOrder_Id(orderId);

        if (paymentOrders.isEmpty()) {
            throw new ApiException("해당 주문의 결제 이력이 없습니다.", ErrorType.NOT_FOUND);
        }

        // 2. Payment 중복 제거 후 paymentKey만 추출
        List<String> paymentKeys = paymentOrders.stream()
            .map(po -> po.getPayment().getPaymentKey())
            .distinct()
            .toList();

        // 3. 각 paymentKey로 Toss API 조회 후 DTO 변환
        return paymentKeys.stream()
            .map(paymentKey -> {
                TossSinglePaymentLookupResponse tossResponse =
                    tossPaymentClient.requestSinglePaymentLookup(paymentKey);

                return PaymentLookupResponse.convertFromTossLookup(tossResponse);
            })
            .toList();
    }
}
