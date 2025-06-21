package org.example.oshipserver.domain.order.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.order.dto.bulk.*;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
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
public class OrderBulkV3Service {

    private final IOrderRepository orderRepository;
    private final IOrderJdbcRepository orderJdbcRepository;
    private final OrderDtoMapper orderDtoMapper;

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

        // 2. masterNo 일괄 생성
        CountryCode countryCode = requests.get(0).request().recipientCountryCode();
        List<String> masterNos = generateUniqueMasterNos(requests.size(), countryCode);
        Iterator<String> masterNoIter = masterNos.iterator();

        // 3. OrderBulkDto 생성 및 매핑
        List<OrderBulkDto> orderDtos = new ArrayList<>();
        Map<String, InternalOrderCreateDto> masterNoToDto = new HashMap<>();

        for (InternalOrderCreateDto dto : requests) {
            OrderCreateRequest req = dto.request();
            String masterNo = masterNoIter.next();
            orderDtos.add(orderDtoMapper.toOrderDto(req, masterNo, dto.sellerId()));
            masterNoToDto.put(masterNo, dto);
        }

        // 4. 주문 INSERT
        orderJdbcRepository.bulkInsertOrders(orderDtos);

        // 5. 주소 DTO 생성 및 INSERT
        List<SenderAddressBulkDto> senderAddresses = masterNos.stream()
            .map(m -> orderDtoMapper.toSenderAddressDto(masterNoToDto.get(m).request(), m))
            .toList();
        List<RecipientAddressBulkDto> recipientAddresses = masterNos.stream()
            .map(m -> orderDtoMapper.toRecipientAddressDto(masterNoToDto.get(m).request(), m))
            .toList();
        orderJdbcRepository.bulkInsertSenderAddresses(senderAddresses);
        orderJdbcRepository.bulkInsertRecipientAddresses(recipientAddresses);

        // 6. ID 매핑 조회
        Map<String, Long> masterNoToOrderId = orderJdbcRepository.findOrderIdMapByMasterNos(masterNos);
        Map<String, Long> masterNoToSenderAddressId = orderJdbcRepository.findSenderAddressIdsByMasterNos(masterNos);
        Map<String, Long> masterNoToRecipientAddressId = orderJdbcRepository.findRecipientAddressIdsByMasterNos(masterNos);

        // 7. 최종 DTO 매핑
        List<OrderItemBulkDto> items = new ArrayList<>();
        List<OrderSenderBulkDto> senders = new ArrayList<>();
        List<OrderRecipientBulkDto> recipients = new ArrayList<>();

        for (String masterNo : masterNos) {
            InternalOrderCreateDto dto = masterNoToDto.get(masterNo);
            OrderCreateRequest req = dto.request();
            Long orderId = masterNoToOrderId.get(masterNo);

            if (orderId == null) throw new ApiException("orderId 조회 실패: " + masterNo, ErrorType.NOT_FOUND);

            items.addAll(orderDtoMapper.toOrderItemDtos(req, masterNo, orderId));
            senders.add(orderDtoMapper.toSenderDto(req, orderId, masterNoToSenderAddressId.get(masterNo), dto.sellerId()));
            recipients.add(orderDtoMapper.toRecipientDto(req, orderId, masterNoToRecipientAddressId.get(masterNo)));
        }

        // 8. 아이템/발송자/수취인 INSERT
        orderJdbcRepository.bulkInsertOrderItems(items);
        orderJdbcRepository.bulkInsertOrderSenders(senders);
        orderJdbcRepository.bulkInsertOrderRecipients(recipients);

        // 9. 결과 반환
        return masterNos;
    }



    // UUID 기반으로 중복 없는 주문 마스터번호를 여러 개 생성
    public List<String> generateUniqueMasterNos(int count, CountryCode countryCode) {
        String prefix = "OSH";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String country = (countryCode != null) ? countryCode.name() : "XX";

        Set<String> result = new LinkedHashSet<>();
        while (result.size() < count) {
            List<String> candidates = new ArrayList<>();
            while (candidates.size() < (count - result.size())) {
                String uuidSegment = UUID.randomUUID().toString().replace("-", "").substring(0, 7).toUpperCase();
                candidates.add(prefix + date + country + uuidSegment);
            }

            // 이미 DB에 존재하는 master 번호는 제외
            List<String> existing = orderRepository.findExistingMasterNos(candidates);
            for (String masterNo : candidates) {
                if (!existing.contains(masterNo)) {
                    result.add(masterNo);
                }
            }
        }

        return new ArrayList<>(result);
    }
}
