package org.example.oshipserver.client.fedex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.example.oshipserver.domain.shipping.entity.TrackingEvent;
import org.example.oshipserver.client.fedex.enums.TrackingEventEnum;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.example.oshipserver.domain.shipping.repository.TrackingEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    private final TrackingEventRepository trackingEventRepository;

    @PostConstruct
    public void runOnStartup() throws Exception {
        // 애플리케이션 시작 시 한 번 실행
        trackingScheduler();
    }

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void trackingScheduler() throws Exception {
        DateTimeFormatter dtf =  DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
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
                slice = shipmentRepository.findAllByCarrierId(carrierId, pageable);
                List<Shipment> shipments = slice.getContent();
                HashMap<String, Long> trackingOrderId = new HashMap<>();
                List<TrackingInfo> trackingInfos = new ArrayList<>();
                for (Shipment shipment : shipments) {
                    String trackingNo = shipment.getCarrierTrackingNo();
                    Long orderId = shipment.getOrderId();
                    trackingOrderId.put(trackingNo, orderId);
                    TrackingInfo fdxe = new TrackingInfo(null, null, new TrackingNumberInfo(trackingNo, "FDXE"));
                    trackingInfos.add(fdxe);
                }
                FedexTrackingRequest fedexTrackingRequest =
                        new FedexTrackingRequest(false, trackingInfos);
                ResponseEntity<String> response = fedexClient.tracking(token, fedexTrackingRequest);
                parseTrackingResponseAndSave(response,trackingOrderId);
                page++; // 다음 페이지 이동
            } while (slice.hasNext());
        }


    }

    private void parseTrackingResponseAndSave(ResponseEntity<String> response, HashMap<String , Long> trackNoMapOrderId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        List<TrackingEvent> trackingEvents = new ArrayList<>();
        JsonNode completeTrackResults = root
                .path("output")
                .path("completeTrackResults");

        for (JsonNode trackResultItem : completeTrackResults) {
            JsonNode trackResults = trackResultItem.path("trackResults");
            for (JsonNode track : trackResults) {
                // trackingNumber
                String trackingNumber = track
                        .path("trackingNumberInfo")
                        .path("trackingNumber")
                        .asText();
                Long orderId = trackNoMapOrderId.get(trackingNumber);
                // scanEvents
                JsonNode scanEvents = track.path("scanEvents");
                for (JsonNode event : scanEvents) {
                    String derivedStatusCode = event.path("derivedStatusCode").asText();
                    TrackingEventEnum trackingEventEnum = TrackingEventEnum.toOshipEvent(derivedStatusCode);
                    String date = event.path("date").asText();
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
                    LocalDateTime scanEventAt = offsetDateTime.toLocalDateTime();
                    TrackingEvent te = TrackingEvent.builder()
                            .orderId(orderId)
                            .scanEventAt(scanEventAt)
                            .description(trackingEventEnum.getDesc())
                            .event(trackingEventEnum)
                            .build();
                    trackingEvents.add(te);
                }
            }
            List<TrackingEvent> deduplicated = trackingEvents.stream()
                    .filter(event -> !trackingEventRepository.existsByOrderIdAndEvent(event.getOrderId(), event.getEvent()))
                    .toList();
            trackingEventRepository.saveAll(deduplicated);
        }
    }


}
