package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderCreateRequest;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private LocalDateTime orderedAt;
    private LocalDateTime deletedAt;

    // 상태
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private OrderStatus currentStatus;

    // 기타 정보
    private String oshipMasterNo;
    private String lastTrackingEvent;


    // 바코드/운송장 출력 여부
    private Boolean isPrintBarcode;
    private Boolean isPrintAwb;

    // 운송장 출력 시각
    private LocalDateTime awbPrintedAt;

    // 배송 완료 시각 (트래킹 기준)
    private LocalDateTime deliveredAt;

    // 배송 소요 일수 (계산된 값)
    private Integer deliveryDays;

    // 주문 아이템
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Partner와 Seller
    private Long parterId;
    private Long sellerId;

    /*
    추후 Partner와 Seller 추가 시 연관관계 설정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    */

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderSender sender;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
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
        LocalDateTime orderedAt,
        Boolean isDeleted,
        OrderStatus currentStatus
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
        this.orderedAt = orderedAt;
        this.isDeleted = isDeleted;
        this.currentStatus = currentStatus;
    }


    /**
     * 주문 생성 팩토리 메서드 (DTO 기반)
     * @param dto 주문 요청 DTO
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
            .orderedAt(LocalDateTime.now())
            .isDeleted(false)
            .currentStatus(OrderStatus.PENDING)
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

}