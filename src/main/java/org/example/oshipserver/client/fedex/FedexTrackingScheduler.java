package org.example.oshipserver.client.fedex;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.client.fedex.dto.FedexTrackingRequest;
import org.example.oshipserver.client.fedex.dto.TrackingInfo;
import org.example.oshipserver.client.fedex.dto.TrackingNumberInfo;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.enums.CarrierName;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FedexTrackingScheduler {

    private final FedexClient fedexClient;
    private final CarrierRepository carrierRepository;
    private final ShipmentRepository shipmentRepository;
    private final FedexTrackingService fedexTrackingService;


    //오늘로부터 100일전 구하는 함수
    private static String getStartDay() {
        LocalDate hundredDaysAgo = LocalDate.now().minusDays(100);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return hundredDaysAgo.format(formatter);
    }


    @PostConstruct
    public void runOnStartup() throws Exception {
        // 애플리케이션 시작 시 한 번 실행
        trackingScheduler();
    }

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void trackingScheduler() throws Exception {
        DateTimeFormatter dtf =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println("스케쥴러 테스트 " + LocalDateTime.now().format(dtf));
        List<Carrier> carriers = carrierRepository.findAllByName(CarrierName.FEDEX);
        for(Carrier carrier : carriers){
            Long carrierId = carrier.getId();
            String token = fedexClient.requestAccessToken(carrierId);
            //트래킹번호를 쉽먼트 테이블에서 찾는다
            int page = 0;
            int size = 30;
            Slice<Shipment> slice;
            do {
                Pageable pageable = PageRequest.of(page, size);
                slice = shipmentRepository.findAllByCarrierIdAndDeliveredAtIsNull(carrierId, pageable);
                List<Shipment> shipments = slice.getContent();
                HashMap<String, Long> trackingOrderId = new HashMap<>();
                List<TrackingInfo> trackingInfos = new ArrayList<>();
                for (Shipment shipment : shipments) {
                    String trackingNo = shipment.getCarrierTrackingNo();
                    Long orderId = shipment.getOrderId();
                    trackingOrderId.put(trackingNo, orderId);
                    TrackingInfo fdxe = new TrackingInfo(getStartDay(), new TrackingNumberInfo(trackingNo));
                    trackingInfos.add(fdxe);
                }
                FedexTrackingRequest fedexTrackingRequest =
                        new FedexTrackingRequest(true, trackingInfos);
                ResponseEntity<String> response = fedexClient.tracking(token, fedexTrackingRequest);
                fedexTrackingService.parseTrackingResponseAndSave(response,trackingOrderId);
                page++; // 다음 페이지 이동
            } while (slice.hasNext());
        }


    }

}
