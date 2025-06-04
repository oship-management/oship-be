package org.example.oshipserver.domain.shipping.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.global.entity.BaseTimeEntity;

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
    @Column(name = "data")
    private String apiData;

    @Column(name = "url")
    private String awbUrl;

    public static Shipment createShipment(Long orderId, Long carrierId) {
        Shipment shipment = new Shipment();
        shipment.orderId = orderId;
        shipment.carrierId = carrierId;
        return shipment;
    }
}