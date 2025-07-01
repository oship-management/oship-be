package org.example.oshipserver.domain.order.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderExcelUploadV1Controller {

    private static final int ORDER_UPLOAD_THREAD_POOL_SIZE = 10; // 주문 처리용 스레드 풀 개수

    private final ExcelOrderParser excelOrderParser;
    private final OrderService orderService;

    /**
     * 엑셀 파일을 업로드 받아 주문을 생성하는 엔드포인트
     *
     * @param file Multipart 엑셀 파일 (헤더 + 데이터)
     * @return 생성된 주문의 masterNo 목록을 응답
     */
    @OrderExecutionLog
    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<List<OrderCreateResponse>>> uploadOrderExcel(
        Authentication authentication,
        @RequestParam(value = "file", required = false) MultipartFile file ) {
        Long userId = Long.valueOf(authentication.getName()); // 인증 정보에서 userId 추출
        List<OrderExcelRequest> dtos = excelOrderParser.parse(file);

        Map<String, List<OrderExcelRequest>> grouped = dtos.stream()
            .collect(Collectors.groupingBy(OrderExcelRequest::orderNo));

        // 병렬 처리를 위한 ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(ORDER_UPLOAD_THREAD_POOL_SIZE);

        // CompletableFuture로 병렬 처리
        List<CompletableFuture<OrderCreateResponse>> futures = grouped.values().stream()
            .map(group -> CompletableFuture.supplyAsync(() -> {
                OrderCreateRequest request = toOrderCreateRequest(group); // sellerId 하드 코딩 제외 필요
                return new OrderCreateResponse(orderService.createOrder(userId, request));
            }, executor))
            .toList();

        // 모든 작업이 완료될 때까지 기다림
        List<OrderCreateResponse> responses = futures.stream()
            .map(CompletableFuture::join)
            .toList();

        executor.shutdown(); // 사용자 응답 속도 느려짐 , 쿼리가 많이 날라감.

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new BaseResponse<>(201, "엑셀 업로드 주문 생성 완료", responses));
    }

    /**
     * 같은 orderNo를 가진 여러 줄의 엑셀 데이터를 단일 주문 요청 객체로 변환
     *
     * @param group 동일한 주문번호를 가진 OrderExcelRequest 목록
     * @return OrderCreateRequest (아이템 목록 포함)
     */
    private OrderCreateRequest toOrderCreateRequest(List<OrderExcelRequest> group) {
        // 첫 번째 행을 기반으로 주문의 공통 필드 추출
        OrderExcelRequest base = group.get(0);

        // 1. 상품 정보 (OrderItemDto 리스트) 생성
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

        // 2. 주문 정보 객체 생성
        return new OrderCreateRequest(
            base.storePlatform(),
            base.orderNo(),
            base.storeName(),

            // 발송자 정보
            base.senderName(),
            base.senderCompany(),
            base.senderEmail(),
            base.senderPhoneNo(),
            CountryCode.valueOf(base.senderCountryCode()), // 문자열 → enum 변환
            base.senderState(),
            StateCode.valueOf(base.senderState()),         // 문자열 → enum 변환
            base.senderCity(),
            base.senderAddress1(),
            base.senderAddress2(),
            base.senderZipCode(),
            base.senderTaxId(),

            // 수취인 정보
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

            // 상품 목록
            orderItems
        );
    }
}