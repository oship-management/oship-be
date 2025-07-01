package org.example.oshipserver.domain.order.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.order.aop.OrderExecutionLog;
import org.example.oshipserver.domain.order.dto.OrderItemDto;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderExcelRequest;
import org.example.oshipserver.domain.order.dto.response.OrderCreateResponse;
import org.example.oshipserver.domain.order.entity.enums.CountryCode;
import org.example.oshipserver.domain.order.entity.enums.StateCode;
import org.example.oshipserver.domain.order.service.OrderService;
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
@RequestMapping("/api/v1/orders/upload/v0")
@RequiredArgsConstructor
public class OrderExcelUploadV0Controller {

    private final ExcelOrderParser excelOrderParser;
    private final OrderService orderService;

    /**
     * V0: 성능 개선 전 순차 처리 버전
     *
     * @param file Multipart 엑셀 파일
     * @return 생성된 주문 masterNo 목록
     */
    @OrderExecutionLog
    @PostMapping
    public ResponseEntity<BaseResponse<List<OrderCreateResponse>>> uploadOrderExcelV0(
        Authentication authentication,
        @RequestParam(value = "file", required = false) MultipartFile file) {

        Long userId = Long.valueOf(authentication.getName()); // 인증 정보에서 userId 추출
        List<OrderExcelRequest> dtos = excelOrderParser.parse(file);

        // orderNo 기준으로 그룹화
        Map<String, List<OrderExcelRequest>> grouped = dtos.stream()
            .collect(Collectors.groupingBy(OrderExcelRequest::orderNo));

        // 순차적으로 주문 생성
        List<OrderCreateResponse> responses = grouped.values().stream()
            .map(group -> {
                OrderCreateRequest request = toOrderCreateRequest(group);
                return new OrderCreateResponse(orderService.createOrder(userId, request));
            })
            .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new BaseResponse<>(201, "엑셀 업로드 주문 생성 완료 (V0)", responses));
    }

    /**
     * 같은 orderNo 그룹을 단일 OrderCreateRequest로 변환
     *
     * @param group 동일 주문번호 데이터 리스트
     * @return OrderCreateRequest
     */
    private OrderCreateRequest toOrderCreateRequest(List<OrderExcelRequest> group) {
        OrderExcelRequest base = group.get(0);

        List<OrderItemDto> orderItems = group.stream().map(row ->
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
            base.storePlatform(),
            base.orderNo(),
            base.storeName(),
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
            orderItems
        );
    }
}
