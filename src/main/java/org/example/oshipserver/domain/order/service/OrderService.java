package org.example.oshipserver.domain.order.service;

import static org.example.oshipserver.global.config.RedisCacheConfig.CURRENT_MONTH_CACHE;

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
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.example.oshipserver.domain.shipping.service.interfaces.TrackingEventHandler;
import org.example.oshipserver.global.common.response.PageResponseDto;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.cache.annotation.CacheEvict;
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
    private final OrderNotificationService orderNotificationService;
    private final TrackingEventHandler trackingEventHandler;

    public String createOrder(Long userId, OrderCreateRequest orderCreateRequest) {

        /** 중복 주문 확인
         * 동일한 seller_id 내에서는 orderNo가 유일해야 하고,
         * seller_id가 다르면 orderNo 중복을 허용
         **/

        if (orderRepository.existsByOrderNoAndSellerId(orderCreateRequest.orderNo(), userId)) {
            throw new ApiException("해당 계정에 동일한 주문번호가 존재합니다: " + orderCreateRequest.orderNo(), ErrorType.DUPLICATED_ORDER);
        }

        // 중복 피한 Master No 생성
        String masterNo = generateUniqueMasterNo(orderCreateRequest.recipientCountryCode());

        // 1. 주문 생성
        Order order = Order.of(orderCreateRequest, masterNo, userId);

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

        // 8. ORDER 생성 트래킹 이벤트 추가
        trackingEventHandler.handleTrackingEvent(
            order.getId(),
            TrackingEventEnum.ORDER_PLACED,
            ""
        );

//        9. 알림 전송 (이메일 발송)
//        동기 호출
//        orderNotificationService.sendOrderCreatedSync(order);
//
//        비동기 호출
//        orderNotificationService.sendOrderCreatedAsync(order);


        return masterNo;
    }

    public String generateUniqueMasterNo(CountryCode countryCode) {
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
        // 검색 시작일: null인 경우 매우 과거 날짜로 설정 (기본값)
        LocalDate start = (startDate != null)
            ? LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE)
            : LocalDate.of(2000, 1, 1);  // 매우 과거로 기본 설정

        // 검색 종료일: null인 경우 오늘 날짜로 설정 (기본값)
        LocalDate end = (endDate != null)
            ? LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE)
            : LocalDate.now();  // 오늘까지로 기본 설정

        // 삭제되지 않은 주문 중, sellerId와 날짜 조건에 맞는 주문 페이지 조회
        Page<Order> orders = orderRepository.findBySellerIdAndCreatedAtBetweenAndDeletedFalse(
            sellerId,
            start.atStartOfDay(),
            end.plusDays(1).atStartOfDay(), // 종료일 포함을 위해 하루 더함
            pageable
        );

        return PageResponseDto.toDto(orders.map(OrderListResponse::from));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        if (order.isDeleted()) {
            throw new ApiException("삭제된 주문입니다.", ErrorType.NOT_FOUND);
        }

        if (!order.getSellerId().equals(userId)) {
            throw new ApiException("해당 주문에 접근할 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        return OrderDetailResponse.from(order);
    }


    @Transactional
    @CacheEvict(
        value = {CURRENT_MONTH_CACHE, "sellerStats"}, // Redis + Local 캐시 모두 무효화
        key = "T(org.example.oshipserver.global.common.utils.CacheKeyUtil).getRedisCurrentMonthStatsKey(#request.sellerId)"
    )
    public void updateOrder(Long userId, Long orderId, OrderUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        if (!order.getSellerId().equals(userId)) {
            throw new ApiException("해당 주문에 접근할 권한이 없습니다.", ErrorType.FORBIDDEN);
        }

        if (order.isDeleted()) {
            throw new ApiException("이미 삭제된 주문입니다.", ErrorType.DB_FAIL);
        }

        // 주문 정보 수정
        order.updateFrom(request);

        // 송신자/수신자 정보 갱신 (기존 객체가 있다고 전제)
        order.getSender().updateFrom(request);
        order.getRecipient().updateFrom(request);

        // 아이템 목록 갱신 (수정/추가/삭제)
        order.updateItems(request.orderItems());
    }

    @Transactional
    public void softDeleteOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiException("주문을 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        if (order.isDeleted()) {
            throw new ApiException("이미 삭제된 주문입니다.", ErrorType.DB_FAIL);
        }

        if (!order.getSellerId().equals(userId)) {
            throw new ApiException("해당 주문에 접근할 권한이 없습니다.", ErrorType.FORBIDDEN);
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