package org.example.oshipserver.client.fedex.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrackingEventEnum {
    // 주문 접수
    ORDER_PLACED("Order placed"),
    // 바코드 생성
    LABEL_CREATED("Label created"),
    // AWB 생성
    AWB_CREATED("Preparing to ship"),
    // AWB 취소
    AWB_CANCEL("AWB Cancel"),
    // GEM 집하지 도착
    CENTER_ARRIVED("Partner center arrived"),
    // 픽업 지연
    PICKUP_DELAY("Delays in pickup"),
    // 운송 준비
    READY_SHIP("Ready to ship"),
    // 운송 시작
    SHIPPED("Shipped"),
    // 목적 국가 도착
    DEST_COUNTRY_ARRIVED("Arrived in destination country"),
    // 통관 지연
    CLEARANCE_DELAY("Delay in clearance"),
    // 통관 중
    IN_CLEARANCE("Customs clearance in progress"),
    // 통관 완료
    CLEARED("Customs clearance completed"),
    // 최종 목적지 이동 중
    FINAL_DEST_TRANSIT("In transit to final destination"),
    // 최종 목적지 도착
    FINAL_DEST_ARRIVED("Arrived at final destination"),
    // 운송 중
    IN_TRANSIT("In transit"),
    // 허브 도착
    HUB_ARRIVED("Arrived at Hub"),
    // 배송 지연
    DELIVERY_DELAY("Delays in delivery"),
    // 배송 예외 사항
    DELIVERY_EXCEPTION("Delays in delivery"),
    // 배송 중
    IN_DELIVERY("Out for delivery"),
    // 배송 완료
    DELIVERED("Delivered"),
    // 반송
    RETURN("Return");

    private final String desc;

    public static TrackingEventEnum toOshipEvent(String eventCode) {
        TrackingEventEnum oshipEvent = null;

        switch(eventCode) {
            case "AF": oshipEvent = TrackingEventEnum.FINAL_DEST_TRANSIT; break;
            case "AR": oshipEvent = TrackingEventEnum.FINAL_DEST_ARRIVED; break;
            case "DD": oshipEvent = TrackingEventEnum.DELIVERY_DELAY; break;
            case "DE": oshipEvent = TrackingEventEnum.DELIVERY_EXCEPTION; break;
            case "DL": oshipEvent = TrackingEventEnum.DELIVERED; break;
            case "DP": oshipEvent = TrackingEventEnum.SHIPPED; break;
            case "IT": oshipEvent = TrackingEventEnum.IN_TRANSIT; break;
            case "OD": oshipEvent = TrackingEventEnum.IN_DELIVERY; break;
            case "PD": oshipEvent = TrackingEventEnum.PICKUP_DELAY; break;
            case "PU": oshipEvent = TrackingEventEnum.READY_SHIP; break;
            case "CC": oshipEvent = TrackingEventEnum.CLEARED; break;
            case "CD": oshipEvent = TrackingEventEnum.CLEARANCE_DELAY; break;
            case "CP": oshipEvent = TrackingEventEnum.IN_CLEARANCE; break;
        }

        return oshipEvent;
    }

}

