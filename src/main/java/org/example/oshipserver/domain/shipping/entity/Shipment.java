package org.example.oshipserver.domain.shipping.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.oshipserver.global.entity.BaseTimeEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "carrier_id", nullable = false)
    private Long carrierId;

    @Column(name = "charge_value", precision = 20, scale = 2)
    private BigDecimal chargeValue;

    @Column(name = "carrier_tracking_no", length = 50)
    private String carrierTrackingNo;

    // 패키지 치수 정보
    @Column(name = "width", precision = 20, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 20, scale = 2)
    private BigDecimal height;

    @Column(name = "length", precision = 20, scale = 2)
    private BigDecimal length;

    // 중량 정보
    @Column(name = "gross_weight", precision = 20, scale = 2)
    private BigDecimal grossWeight;

    @Column(name = "volumn_weight", precision = 20, scale = 2)
    private BigDecimal volumeWeight;

    @Column(name = "charge_weight", precision = 20, scale = 2)
    private BigDecimal chargeWeight;

    // API 관련 정보
    @Column(name = "data", length = 1000)
    private String apiData;

    @Column(name = "url", length = 500)
    private String awbUrl;

    //배송완료를 나타내는 필드
    @Setter
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Builder
    private Shipment(
        Long orderId,
        Long carrierId,
        BigDecimal chargeValue,
        String carrierTrackingNo,
        BigDecimal width,
        BigDecimal height,
        BigDecimal length,
        BigDecimal grossWeight,
        BigDecimal volumeWeight,
        BigDecimal chargeWeight,
        String apiData,
        String awbUrl
    ) {
        this.orderId = orderId;
        this.carrierId = carrierId;
        this.chargeValue = chargeValue;
        this.carrierTrackingNo = carrierTrackingNo;
        this.width = width;
        this.height = height;
        this.length = length;
        this.grossWeight = grossWeight;
        this.volumeWeight = volumeWeight;
        this.chargeWeight = chargeWeight;
        this.apiData = apiData;
        this.awbUrl = awbUrl;
    }

    // 정적 팩토리 메서드 (기존 방식 유지)
    public static Shipment createShipment(Long orderId, Long carrierId) {
        return Shipment.builder()
            .orderId(orderId)
            .carrierId(carrierId)
            .build();
    }

    // 측정 정보 업데이트 메서드
    public void updateMeasurements(BigDecimal width, BigDecimal height, BigDecimal length, BigDecimal grossWeight) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.grossWeight = grossWeight;
    }

    // AWB URL 업데이트 메서드
    public void updateAwb(String awbUrl, String carrierTrackingNo) {
        this.awbUrl = awbUrl;
        this.carrierTrackingNo = carrierTrackingNo;
    }
}