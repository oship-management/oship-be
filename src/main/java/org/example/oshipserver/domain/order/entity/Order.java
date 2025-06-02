package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private String trackingLevel;

    // 운송정보 추후 연관관계 설정
    private Long shipmentId;
    private String trackingEvent;

    // 바코드/운송장 출력 여부
    private Boolean isPrintBarcode;
    private Boolean isPrintAwb;

    // 운송장 출력 시각
    private LocalDateTime awbPrintedAt;

    // 배송 완료 시각 (트래킹 기준)
    private LocalDateTime deliveredAt;

    // 배송 소요 일수 (계산된 값)
    private Integer deliveryDays;

    // 결제 상태
    private String paymentStatus;

    // 주문 아이템
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    /*
    추후 Partner와 Seller 추가 시 연관관계 설정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    */
}
