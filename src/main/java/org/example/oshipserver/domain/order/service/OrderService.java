package org.example.oshipserver.domain.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.vo.CustomUserDetail;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderUpdateRequest;
import org.example.oshipserver.domain.order.dto.response.OrderDetailResponse;
import org.example.oshipserver.domain.order.dto.response.OrderListResponse;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.OrderItem;
import org.example.oshipserver.domain.order.entity.OrderRecipient;
import org.example.oshipserver.domain.order.entity.OrderSender;
import org.example.oshipserver.domain.order.entity.RecipientAddress;
import org.example.oshipserver.domain.order.entity.SenderAddress;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.entity.enums.DeleterRole;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.global.common.response.PageResponseDto;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public String createOrder(OrderCreateRequest orderCreateRequest) {

        /** 중복 주문 확인
         * 동일한 seller_id 내에서는 orderNo가 유일해야 하고,
         * seller_id가 다르면 orderNo 중복을 허용
         **/
        Long sellerId = orderCreateRequest.sellerId();

        if (orderRepository.existsByOrderNoAndSellerId(orderCreateRequest.orderNo(), sellerId)) {
            throw new ApiException("해당 계정에 동일한 주문번호가 존재합니다: " + orderCreateRequest.orderNo(), ErrorType.DUPLICATED_ORDER);
        }

        // 중복 피한 Master No 생성
        String masterNo = generateUniqueMasterNo(orderCreateRequest.recipientCountryCode());

        // 1. 주문 생성
        Order order = Order.of(orderCreateRequest, masterNo);

        // 2. 아이템 생성
        List<OrderItem> items = orderCreateRequest.orderItems().stream()
            .map(OrderItem::of)
            .toList();
        order.addItems(items);

        // 3. 발송자 주소 생성
        SenderAddress senderAddress = SenderAddress.builder()
            .senderCountryCode(orderCreateRequest.senderCountryCode())
            .senderState(orderCreateRequest.senderState())
            .senderStateCode(orderCreateRequest.senderStateCode())
            .senderCity(orderCreateRequest.senderCity())
            .senderAddress1(orderCreateRequest.senderAddress1())
            .senderAddress2(orderCreateRequest.senderAddress2())
            .senderZipCode(orderCreateRequest.senderZipCode())
            .senderTaxId(orderCreateRequest.senderTaxId())
            .build();

        // 4. 발송자 생성
        OrderSender sender = OrderSender.builder()
            .storePlatform(orderCreateRequest.storePlatform())
            .storeName(orderCreateRequest.storeName())
            .senderName(orderCreateRequest.senderName())
            .senderCompany(orderCreateRequest.senderCompany())
            .senderEmail(orderCreateRequest.senderEmail())
            .senderPhoneNo(orderCreateRequest.senderPhoneNo())
            .senderAddress(senderAddress)
            .build();
        sender.assignOrder(order);
        order.assignSender(sender);

        // 5. 수취인 주소 생성
        RecipientAddress recipientAddress = RecipientAddress.builder()
            .recipientCountryCode(orderCreateRequest.recipientCountryCode())
            .recipientState(orderCreateRequest.recipientState())
            .recipientStateCode(orderCreateRequest.recipientStateCode())
            .recipientCity(orderCreateRequest.recipientCity())
            .recipientAddress1(orderCreateRequest.recipientAddress1())
            .recipientAddress2(orderCreateRequest.recipientAddress2())
            .recipientZipCode(orderCreateRequest.recipientZipCode())
            .recipientTaxId(orderCreateRequest.recipientTaxId())
            .build();

        // 6. 수취인 생성
        OrderRecipient recipient = OrderRecipient.builder()
            .recipientName(orderCreateRequest.recipientName())
            .recipientCompany(orderCreateRequest.recipientCompany())
            .recipientEmail(orderCreateRequest.recipientEmail())
            .recipientPhoneNo(orderCreateRequest.recipientPhoneNo())
            .recipientAddress(recipientAddress)
            .build();
        recipient.assignOrder(order);
        order.assignRecipient(recipient);

        // 7. 저장
        orderRepository.save(order);
        return masterNo;
    }

    private String generateUniqueMasterNo(CountryCode countryCode) {
        String prefix = "OSH";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String country = (countryCode != null) ? countryCode.name() : "XX";

        String masterNo;
        do {
            String uuidSegment = UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
            masterNo = prefix + date + country + uuidSegment;
        } while (orderRepository.existsByOshipMasterNo(masterNo));

        return masterNo;
    }

    @Transactional(readOnly = true)
    public PageResponseDto<OrderListResponse> getOrderList(
        Long sellerId, String startDate, String endDate, Pageable pageable
    ) {
        // 날짜 파싱 (nullable 허용)
        LocalDate start = (startDate != null)
            ? LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
            : LocalDate.of(2000, 1, 1);  // 매우 과거로 기본 설정

        LocalDate end = (endDate != null)
            ? LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
            : LocalDate.now();  // 오늘까지로 기본 설정

        Page<Order> orders;

        if (sellerId == null) {
            // sellerId 없이 전체 조회 (날짜 조건만)
            orders = orderRepository.findByCreatedAtBetween(
                start.atStartOfDay(), end.plusDays(1).atStartOfDay(), pageable);
        } else {
            // sellerId와 날짜 조건 모두 사용
            orders = orderRepository.findBySellerIdAndCreatedAtBetween(
                sellerId, start.atStartOfDay(), end.plusDays(1).atStartOfDay(), pageable);
        }

        return PageResponseDto.toDto(orders.map(OrderListResponse::from));
    }


    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        return OrderDetailResponse.from(order);
    }

    @Transactional
    public void updateOrder(Long orderId, OrderUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        if (order.isDeleted()) {
            throw new ApiException("이미 삭제된 주문입니다.", ErrorType.DB_FAIL);
        }

        order.updateFrom(request);

        if (order.getSender() != null) {
            order.getSender().updateFrom(request);
        }

        if (order.getRecipient() != null) {
            order.getRecipient().updateFrom(request);
        }

        order.updateItems(request.orderItems());
    }

    @Transactional
    public void softDeleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        if (order.isDeleted()) {
            throw new ApiException("이미 삭제된 주문입니다.", ErrorType.DB_FAIL);
        }

        // 현재 로그인 사용자 정보에서 삭제 주체 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (!(principal instanceof CustomUserDetail userDetail)) {
            throw new ApiException("삭제자 정보가 없습니다.", ErrorType.NOT_FOUND);
        }

        DeleterRole role = switch (userDetail.getUserRole()) {
            case SELLER -> DeleterRole.SELLER;
            case PARTNER -> DeleterRole.PARTNER;
            case ADMIN -> DeleterRole.ADMIN;
        };

        order.softDelete(role);
    }

}