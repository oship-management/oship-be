package org.example.oshipserver.domain.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.order.entity.OrderItem;
import org.example.oshipserver.domain.order.entity.OrderRecipient;
import org.example.oshipserver.domain.order.entity.OrderSender;
import org.example.oshipserver.domain.order.entity.RecipientAddress;
import org.example.oshipserver.domain.order.entity.SenderAddress;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.repository.OrderRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;

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
            .senderEmail(orderCreateRequest.senderEmail())
            .senderPhoneNo(orderCreateRequest.senderPhoneNo())
            .address(senderAddress)
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

    // 중복 방지 Master No 생성기
    private String generateUniqueMasterNo(CountryCode countryCode) {
        int maxRetries = 5;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String candidate = generateMasterNo(countryCode);
            if (!orderRepository.existsByMasterNo(candidate)) {
                return candidate;
            }
        }
        throw new ApiException("MasterNo 생성 실패: 중복 회피 불가", ErrorType.FAIL);
    }

    // 기본 Master No 생성 메서드
    private String generateMasterNo(CountryCode countryCode) {
        String prefix = "OSH";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String country = countryCode != null ? countryCode.name() : "XX";
        int randomNumber = 1000000 + new Random().nextInt(9000000);
        return prefix + date + country + randomNumber;
    }

}


