package org.example.oshipserver.client.fedex.enums;

import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

public enum FedexTrackingEnums {

    AA("공항에 있음","At Airport"),
    AC("Canada Post 기관", "At Canada Post facility"),
    AD("배송 중", "At Delivery"),
    AF("FedEx 시설에 있음", "At FedEx Facility"),
    AO("발송물 정시 도착", "Shipment arriving On-time"),
    AP("픽업 장소", "At Pickup"),
    AR("도착", "Arrived"),
    AX("USPS 시설에 있음", "At USPS facility"),
    CA("발송 취소됨", "Shipment Cancelled"),
    CH("위치 변경됨", "Location Changed"),
    DD("배송 지연", "Delivery Delay"),
    DE("배송 예외 사항", "Delivery Exception"),
    DL("배송 완료", "Delivered"),
    DP("출발함", "Departed"),
    DR("차량 준비되었으나 사용되지 않음", "Vehicle furnished but not used"),
    DS("차량 출발함", "Vehicle Dispatched"),
    DY("지연", "Delay"),
    EA("공항으로 운송 중", "Enroute to Airport"),
    ED("배송지로 운송 중", "Enroute to Delivery"),
    EO("출발지 공항으로 운송 중", "Enroute to Origin Airport"),
    EP("픽업 장소로 운송 중", "Enroute to Pickup"),
    FD("도착지 FedEx 시설에 있음", "At FedEx Destination"),
    HL("FedEx 접수처에 보관", "Hold at Location"),
    IT("운송 중", "In Transit"),
    IX("배송 중(세부 정보 참조)", "In transit (see Details)"),
    LO("출발지를 떠남", "Left Origin"),
    OC("주문 생성됨", "Order Created"),
    OD("배송 중", "Out for Delivery"),
    OF("FedEx 출발지 시설에 있음", "At FedEx origin facility"),
    OX("발송 정보가 USPS로 전달됨", "Shipment information sent to USPS"),
    PD("픽업 지연", "Pickup Delay"),
    PF("항공 운송 중", "Plane in Flight"),
    PL("항공편 착륙", "Plane Landed"),
    PM("진행 중", "In Progress"),
    PU("픽업됨", "Picked Up"),
    PX("픽업됨(세부 정보 참조)", "Picked up (see Details)"),
    RR("CDO 요청됨", "CDO requested"),
    RM("CDO 수정됨", "CDO Modified"),
    RC("CDO 취소됨", "CDO Cancelled"),
    RS("발송인에게 반송", "Return to Shipper"),
    RP("반송 라벨 링크가 반송 발송인에게 이메일로 전달됨", "Return label link emailed to return sender"),
    LP("반송 라벨 링크가 발송 생성자에 의해 취소됨", "Return label link cancelled by shipment originator"),
    RG("반송 라벨 링크가 곧 만료됨", "Return label link expiring soon"),
    RD("반송 라벨 링크가 만료됨", "Return label link expired"),
    SE("발송 예외 사항", "Shipment Exception"),
    SF("분류 시설에 있음", "At Sort Facility"),
    SP("분할 상태", "Split Status"),
    TR("인계", "Transfer"),

    // Regulatory
    CC("통관 완료됨", "Cleared Customs"),
    CD("통관 지연", "Clearance Delay"),
    CP("통관 진행 중", "Clearance in Progress");
    // EA("수출 승인됨", "Export Approved"),
    // SP("분할 상태", "Split Status"),

    // Possession
    // CA("운송업체", "Carrier"),
    // RC("수취인", "Recipient"),
    // SH("발송인", "Shipper"),
    // CU("세관", "Customs"),
    // BR("브로커", "Broker"),
    // TP("인계 협력사", "Transfer Partner"),
    // SP("분할 상태", "Split status");

    private final String descKor;
    private final String desc;

    FedexTrackingEnums(String descKor, String desc) {
        this.descKor = descKor;
        this.desc = desc;
    }

    public static TrackingEventEnum toTrackingEvent(String eventCode) {
        TrackingEventEnum trackingEvent = null;

        switch(eventCode) {
            // case "AA": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "AC": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "AD": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "AF":trackingEvent= TrackingEventEnum.FINAL_DEST_TRANSIT; break;
            // case "AO": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "AP": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "AR": trackingEvent = TrackingEventEnum.FINAL_DEST_ARRIVED; break;
            // case "AX": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "CA": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "CH": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "DD": trackingEvent = TrackingEventEnum.DELIVERY_DELAY; break;
            case "DE": trackingEvent = TrackingEventEnum.DELIVERY_EXCEPTION; break;
            case "DL": trackingEvent = TrackingEventEnum.DELIVERED; break;
            case "DP": trackingEvent = TrackingEventEnum.SHIPPED; break;
            // case "DR": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "DS": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "DY": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "EA": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "ED": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "EO": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "EP": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "FD": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "HL": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "IT": trackingEvent = TrackingEventEnum.IN_TRANSIT; break;
            // case "IX": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "LO": trackingEvent = TrackingEventEnum.SHIPPED; break;
            // case "OC": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "OD": trackingEvent = TrackingEventEnum.IN_DELIVERY; break;
            // case "OF": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "OX": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "PD": trackingEvent = TrackingEventEnum.PICKUP_DELAY; break;
            // case "PF": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "PL": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "PM": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            case "PU": trackingEvent = TrackingEventEnum.READY_SHIP; break;
            // case "PX": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RR": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RM": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RC": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RS": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RP": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "LP": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RG": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "RD": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "SE": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "SF": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "SP": trackingEvent = TrackingEventEnum.UNKNOWN; break;
            // case "TR": trackingEvent = TrackingEventEnum.UNKNOWN; break;

            case "CC": trackingEvent = TrackingEventEnum.CLEARED; break;
            case "CD": trackingEvent = TrackingEventEnum.CLEARANCE_DELAY; break;
            case "CP": trackingEvent = TrackingEventEnum.IN_CLEARANCE; break;
            // case "EA": trackingEvent = "수출 승인됨"; break;
            // case "SP": trackingEvent = "분할 상태"; break;

            // Possession
            // CA("운송업체", "Carrier"),
            // RC("수취인", "Recipient"),
            // SH("발송인", "Shipper"),
            // CU("세관", "Customs"),
            // BR("브로커", "Broker"),
            // TP("인계 협력사", "Transfer Partner"),
            // SP("분할 상태", "Split status");
        }

        return trackingEvent;
    }
}
