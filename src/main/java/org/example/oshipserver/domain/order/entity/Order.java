package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.domain.order.dto.request.OrderItemRequest;
import org.example.oshipserver.domain.order.dto.request.OrderUpdateRequest;
import org.example.oshipserver.domain.order.entity.enums.DeleterRole;
import org.example.oshipserver.domain.order.entity.enums.OrderStatus;
import org.example.oshipserver.global.entity.BaseTimeEntity;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNo;

    // 수량 및 중량
    private int parcelCount;
    private BigDecimal shipmentActualWeight;
    private BigDecimal shipmentVolumeWeight;
    private String weightUnit;

    // 부피 정보
    private BigDecimal dimensionWidth;
    private BigDecimal dimensionHeight;
    private BigDecimal dimensionLength;

    // 시간 정보
    private LocalDateTime deletedAt;

    // 상태
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    @CreatedBy // 삭제 주체 자동 주입
    private DeleterRole deletedBy;

    @Enumerated(EnumType.STRING)
    private OrderStatus currentStatus;

    // 기타 정보
    private String oshipMasterNo;
    private String lastTrackingEvent;

    // 물품 종류
    private String itemContentsType;

    // 배송비 정책
    private String serviceType;

    // 포장 방식
    private String packageType;

    // 통관 조건
    private String shippingTerm;

    // 바코드/운송장 출력 여부
    private Boolean isPrintBarcode = false;
    private Boolean isPrintAwb = false;

    // 배송 완료 시각 (트래킹 기준)
    private LocalDateTime deliveredAt;

    // 배송 소요 일수 (계산된 값)
    private Integer deliveryDays;

    // 주문 아이템
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Partner와 Seller
    private Long partnerId;
    private Long sellerId;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderSender sender;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderRecipient recipient;

    @Builder
    private Order(
        String orderNo,
        String oshipMasterNo,
        String weightUnit,
        int parcelCount,
        BigDecimal shipmentActualWeight,
        BigDecimal shipmentVolumeWeight,
        BigDecimal dimensionWidth,
        BigDecimal dimensionHeight,
        BigDecimal dimensionLength,
        boolean deleted,
        OrderStatus currentStatus,
        String itemContentsType,
        String serviceType,
        String packageType,
        String shippingTerm,
        Long sellerId
    ) {
        this.orderNo = orderNo;
        this.oshipMasterNo = oshipMasterNo;
        this.weightUnit = weightUnit;
        this.parcelCount = parcelCount;
        this.shipmentActualWeight = shipmentActualWeight;
        this.shipmentVolumeWeight = shipmentVolumeWeight;
        this.dimensionWidth = dimensionWidth;
        this.dimensionHeight = dimensionHeight;
        this.dimensionLength = dimensionLength;
        this.currentStatus = currentStatus;
        this.itemContentsType = itemContentsType;
        this.serviceType = serviceType;
        this.packageType = packageType;
        this.shippingTerm = shippingTerm;
        this.sellerId = sellerId;
        this.deleted = false;
        this.deletedBy = null;
        this.deletedAt = null;
    }

    /**
     * 주문 생성 팩토리 메서드 (DTO 기반)
     *
     * @param dto      주문 요청 DTO
     * @param masterNo 외부 식별자
     */
    public static Order of(OrderCreateRequest dto, String masterNo) {
        return Order.builder()
            .orderNo(dto.orderNo())
            .oshipMasterNo(masterNo)
            .parcelCount(dto.parcelCount())
            .shipmentActualWeight(BigDecimal.valueOf(dto.shipmentActualWeight()))
            .shipmentVolumeWeight(BigDecimal.valueOf(dto.shipmentVolumeWeight()))
            .weightUnit(dto.weightUnit())
            .dimensionWidth(BigDecimal.valueOf(dto.dimensionWidth()))
            .dimensionHeight(BigDecimal.valueOf(dto.dimensionHeight()))
            .dimensionLength(BigDecimal.valueOf(dto.dimensionLength()))
            .deleted(false)
            .currentStatus(OrderStatus.PENDING)
            .itemContentsType(dto.itemContentsType())
            .serviceType(dto.serviceType())
            .packageType(dto.packageType())
            .shippingTerm(dto.shippingTerm())
            .sellerId(dto.sellerId())
            .build();
    }

    /**
     * 주문 상품 목록 추가
     */
    public void addItems(List<OrderItem> items) {
        this.orderItems.addAll(items);
        items.forEach(item -> item.assignOrder(this));
    }

    public void assignSender(OrderSender sender) {
        this.sender = sender;
    }

    public void assignRecipient(OrderRecipient recipient) {
        this.recipient = recipient;
    }

    public void assignPartner(Long partnerId) {
        this.partnerId = partnerId;
    }

    public void softDelete(DeleterRole deletedBy) {
        this.deleted = true;
        this.deletedBy = deletedBy;
        this.deletedAt = LocalDateTime.now();
    }

    // 주문 정보 갱신
    public void updateFrom(OrderUpdateRequest req) {
        this.orderNo = req.orderNo();
        this.parcelCount = req.parcelCount();
        this.shipmentActualWeight = BigDecimal.valueOf(req.shipmentActualWeight());
        this.shipmentVolumeWeight = BigDecimal.valueOf(req.shipmentVolumeWeight());
        this.weightUnit = req.weightUnit();
        this.dimensionWidth = BigDecimal.valueOf(req.dimensionWidth());
        this.dimensionHeight = BigDecimal.valueOf(req.dimensionHeight());
        this.dimensionLength = BigDecimal.valueOf(req.dimensionLength());
        this.itemContentsType = req.itemContentsType();
        this.serviceType = req.serviceType();
        this.packageType = req.packageType();
        this.shippingTerm = req.shippingTerm();
    }

    public void updateItems(List<OrderItemRequest> updatedRequests) {
        // 1. 기존 아이템들을 ID 기준으로 Map화
        Map<Long, OrderItem> existingItemMap = this.orderItems.stream()
            .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        List<OrderItem> newItemList = new ArrayList<>();

        for (OrderItemRequest req : updatedRequests) {
            Long id = req.id();

            if (id != null) {
                OrderItem existing = existingItemMap.get(id);
                if (existing == null) {
                    throw new ApiException("존재하지 않는 주문 항목입니다. id=" + id, ErrorType.NOT_FOUND);
                }
                existing.updateFrom(req);
                newItemList.add(existing);
                existingItemMap.remove(id); // 삭제 대상에서 제외
            } else {
                OrderItem newItem = req.toEntity(); // 신규 생성
                newItem.assignOrder(this);
                newItemList.add(newItem);
            }
        }

        // 2. 기존 orderItems 중 요청에 포함되지 않은 항목은 제거됨
        this.orderItems.clear();
        this.orderItems.addAll(newItemList);
    }


    // 주문이 삭제되었는지 확인
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * 마지막 트래킹 이벤트 업데이트
     */
    public void updateLastTrackingEvent(String eventName) {
        this.lastTrackingEvent = eventName;
    }

    /**
     * 바코드 생성 완료 처리
     */
    public void markBarcodeGenerated() {
        this.isPrintBarcode = true;
    }

    /**
     * AWB 생성 완료 처리
     */
    public void markAwbGenerated() {
        this.isPrintAwb = true;
    }

    /**
     * 결제 상태에 따른, 주문 상태 업데이트
     */
    public void markAsPaid() {
        this.currentStatus = OrderStatus.PAID;
    }
    public void markAsCancelled() {
        this.currentStatus = OrderStatus.CANCELLED;
    }
    public void markAsRefunded() {
        this.currentStatus = OrderStatus.REFUNDED;
    }
    public void markAsFailed() {
        this.currentStatus = OrderStatus.FAILED;
    }
}