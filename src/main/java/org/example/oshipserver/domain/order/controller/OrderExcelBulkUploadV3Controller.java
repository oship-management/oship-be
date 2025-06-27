package org.example.oshipserver.domain.order.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.aop.OrderExecutionLog;
import org.example.oshipserver.domain.order.dto.OrderItemDto;
import org.example.oshipserver.domain.order.dto.bulk.InternalOrderCreateDto;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderExcelRequest;
import org.example.oshipserver.domain.order.dto.response.OrderCreateResponse;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.entity.enums.StateCode;
import org.example.oshipserver.domain.order.service.OrderBulkV3Service;
import org.example.oshipserver.domain.order.util.ExcelOrderParser;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v3/orders")
@RequiredArgsConstructor
public class OrderExcelBulkUploadV3Controller {

    private final OrderBulkV3Service orderBulkV3Service;
    private final ExcelOrderParser excelOrderParser;

    /**
     * 엑셀 파일을 업로드 받아 여러 주문을 생성하는 엔드포인트
     * - 주문번호(orderNo)를 기준으로 주문을 그룹화
     * - 중복 주문번호가 하나라도 있으면 전체 실패
     * - 한 주문 안에 여러 상품이 포함될 수 있음
     */
    @OrderExecutionLog
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<List<OrderCreateResponse>>> uploadBulkOrderExcel(
        Authentication authentication,
        @RequestParam("file") MultipartFile file) {

        // 1. 로그인한 사용자 ID 추출
        Long sellerId = Long.valueOf(authentication.getName());

        // 2. 엑셀 파싱 → OrderExcelRequest 목록 생성
        List<OrderExcelRequest> dtos = excelOrderParser.parse(file);

        // 3. 주문번호 기준으로 그룹화
        Map<String, List<OrderExcelRequest>> grouped = dtos.stream()
            .collect(Collectors.groupingBy(OrderExcelRequest::orderNo));

        // 4. 그룹별로 InternalOrderCreateDto로 변환 (sellerId 포함)
        List<InternalOrderCreateDto> requests = grouped.values().stream()
            .map(group -> new InternalOrderCreateDto(sellerId, toOrderCreateRequest(group)))
            .toList();

        // 5. 주문 생성 처리
        List<String> masterNos = orderBulkV3Service.createOrdersBulk(requests);
        List<OrderCreateResponse> responses = masterNos.stream()
            .map(OrderCreateResponse::new)
            .toList();

        // 6. 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new BaseResponse<>(201, "엑셀 업로드 주문 생성 완료", responses));
    }

    /**
     * 동일한 주문번호를 가진 OrderExcelRequest들을 하나의 OrderCreateRequest로 변환
     * - 첫 행을 기준으로 주문/발송자/수취인/배송 정보 추출
     * - 모든 행을 상품 정보(OrderItemDto)로 변환하여 포함
     */
    private OrderCreateRequest toOrderCreateRequest(List<OrderExcelRequest> group) {
        OrderExcelRequest base = group.get(0);  // 그룹 대표 행

        List<OrderItemDto> items = group.stream().map(row ->
            new OrderItemDto(
                row.itemName(),
                row.itemQuantity(),
                row.itemUnitValue(),
                row.itemValueCurrency(),
                row.itemWeight(),
                row.weightUnit(),
                row.itemHSCode(),
                row.itemOriginCountryCode()
            )
        ).toList();

        return new OrderCreateRequest(
            // 주문 정보
            base.storePlatform(),
            base.orderNo(),
            base.storeName(),

            // 발송자
            base.senderName(),
            base.senderCompany(),
            base.senderEmail(),
            base.senderPhoneNo(),
            CountryCode.valueOf(base.senderCountryCode()),
            base.senderState(),
            StateCode.valueOf(base.senderState()),
            base.senderCity(),
            base.senderAddress1(),
            base.senderAddress2(),
            base.senderZipCode(),
            base.senderTaxId(),

            // 수취인
            base.recipientName(),
            base.recipientCompany(),
            base.recipientEmail(),
            base.recipientPhoneNo(),
            CountryCode.valueOf(base.recipientCountryCode()),
            base.recipientState(),
            StateCode.valueOf(base.recipientState()),
            base.recipientCity(),
            base.recipientAddress1(),
            base.recipientAddress2(),
            base.recipientZipCode(),
            base.recipientTaxId(),

            // 배송 정보
            base.itemContentsType(),
            base.parcelCount(),
            base.serviceType(),
            base.shipmentActualWeight(),
            base.shipmentVolumeWeight(),
            base.weightUnit(),
            base.dimensionWidth().intValue(),
            base.dimensionLength().intValue(),
            base.dimensionHeight().intValue(),
            base.packageType(),
            base.shippingTerm(),
            base.lastTrackingEvent(),

            // 아이템 리스트
            items
        );
    }
}
