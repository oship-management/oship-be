package org.example.oshipserver.domain.order.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.order.dto.bulk.InternalOrderCreateDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderItemBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderRecipientBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderSenderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.RecipientAddressBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.SenderAddressBulkDto;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.repository.IOrderRepository;
import org.example.oshipserver.domain.order.repository.jdbc.IOrderJdbcRepository;
import org.example.oshipserver.domain.order.service.bulkmapper.OrderDtoMapper;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderBulkService {

    private final IOrderRepository orderRepository;
    private final IOrderJdbcRepository orderJdbcRepository;
    private final OrderDtoMapper orderDtoMapper;
    private final OrderService orderService;

    @Transactional
    public List<String> createOrdersBulk(List<InternalOrderCreateDto> requests) {

        // 1. 주문번호 중복 확인
        Set<String> incomingOrderNos = requests.stream()
            .map(dto -> dto.request().orderNo())
            .collect(Collectors.toSet());

        Long sellerId = requests.get(0).sellerId();
        List<String> existingOrderNos = orderRepository.findExistingOrderNos(sellerId, new ArrayList<>(incomingOrderNos));
        if (!existingOrderNos.isEmpty()) {
            throw new ApiException("중복된 주문번호 존재: " + existingOrderNos, ErrorType.DUPLICATED_ORDER);
        }

        // 2. OrderBulkDto 생성 및 masterNo → dto 매핑
        List<OrderBulkDto> orderDtos = new ArrayList<>();
        Map<String, InternalOrderCreateDto> masterNoToDto = new HashMap<>();

        for (InternalOrderCreateDto dto : requests) {
            OrderCreateRequest req = dto.request();

            // V3 에서는 generateUniqueMasterNo (다건 메서드 생성으로 변경)
            String masterNo = orderService.generateUniqueMasterNo(req.recipientCountryCode());
            orderDtos.add(orderDtoMapper.toOrderDto(req, masterNo, dto.sellerId()));
            masterNoToDto.put(masterNo, dto);
        }

        // 3. 주문 insert
        orderJdbcRepository.bulkInsertOrders(orderDtos);

        // 4. masterNo 목록 준비
        List<String> masterNos = orderDtos.stream()
            .map(OrderBulkDto::oshipMasterNo)
            .toList();

        // 5. 주소 DTO 생성 및 insert 먼저 수행
        List<SenderAddressBulkDto> senderAddresses = new ArrayList<>();
        List<RecipientAddressBulkDto> recipientAddresses = new ArrayList<>();

        for (String masterNo : masterNos) {
            InternalOrderCreateDto dto = masterNoToDto.get(masterNo);
            OrderCreateRequest req = dto.request();

            senderAddresses.add(orderDtoMapper.toSenderAddressDto(req, masterNo));
            recipientAddresses.add(orderDtoMapper.toRecipientAddressDto(req, masterNo));
        }
        orderJdbcRepository.bulkInsertSenderAddresses(senderAddresses);
        orderJdbcRepository.bulkInsertRecipientAddresses(recipientAddresses);

        // 6. 이제 orderId + addressId 조회
        Map<String, Long> masterNoToOrderId = orderJdbcRepository.findOrderIdMapByMasterNos(masterNos);
        Map<String, Long> masterNoToSenderAddressId = orderJdbcRepository.findSenderAddressIdsByMasterNos(masterNos);
        Map<String, Long> masterNoToRecipientAddressId = orderJdbcRepository.findRecipientAddressIdsByMasterNos(masterNos);

        // 7. 나머지 DTO 생성
        List<OrderItemBulkDto> items = new ArrayList<>();
        List<OrderSenderBulkDto> senders = new ArrayList<>();
        List<OrderRecipientBulkDto> recipients = new ArrayList<>();

        for (String masterNo : masterNos) {
            InternalOrderCreateDto dto = masterNoToDto.get(masterNo);
            OrderCreateRequest req = dto.request();
            Long orderId = masterNoToOrderId.get(masterNo);

            if (orderId == null) {
                throw new ApiException("orderId 조회 실패: " + masterNo, ErrorType.NOT_FOUND);
            }

            Long senderAddressId = masterNoToSenderAddressId.get(masterNo);
            Long recipientAddressId = masterNoToRecipientAddressId.get(masterNo);

            items.addAll(orderDtoMapper.toOrderItemDtos(req, masterNo, orderId));
            senders.add(orderDtoMapper.toSenderDto(req, orderId, senderAddressId, dto.sellerId()));
            recipients.add(orderDtoMapper.toRecipientDto(req, orderId, recipientAddressId));
        }

        // 8. 최종 bulk insert
        orderJdbcRepository.bulkInsertOrderItems(items);
        orderJdbcRepository.bulkInsertOrderSenders(senders);
        orderJdbcRepository.bulkInsertOrderRecipients(recipients);


        // OrderBulkRepository ,,, JDBC 기반의 레포지토리에서 실직적인 Insert 작업
        // 여러 테이블의 insert 메서드를 호출 ,,, 5개의 list DTO 를 넣어서 ....

        // 트래킹 이벤트 등록 (옵션)
//        for (Order order : orders) {
//            trackingEventHandler.handleTrackingEvent(
//                order.getId(),
//                TrackingEventEnum.ORDER_PLACED,
//                ""
//            );
//        }


        // 9. 완료된 masterNo 반환
        return masterNos;
    }

}
