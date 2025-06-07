package org.example.oshipserver.domain.shipping.service.interfaces;

import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;

/**
 * 외부 API 또는 다른 도메인에서 트래킹 이벤트를 생성할 때 사용하는 인터페이스
 */
public interface TrackingEventHandler {

    /**
     * 트래킹 이벤트 생성 (내부 및 외부 공통)
     * 추후 배송외부 API 연동시 추가 (주석으로 대비)
     * @param orderId 주문 ID
     * @param event 이벤트 타입
     * @param description 설명
     */
    void handleTrackingEvent(Long orderId, TrackingEventEnum event, String description);
}
