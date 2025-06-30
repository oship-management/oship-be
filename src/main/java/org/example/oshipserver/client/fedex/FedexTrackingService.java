package org.example.oshipserver.client.fedex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oshipserver.client.fedex.enums.FedexTrackingEnums;
import org.example.oshipserver.domain.shipping.entity.enums.TrackingEventEnum;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.domain.shipping.entity.TrackingEvent;
import org.example.oshipserver.domain.shipping.repository.ShipmentRepository;
import org.example.oshipserver.domain.shipping.repository.TrackingEventRepository;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FedexTrackingService {


    private final ShipmentRepository shipmentRepository;
    private final TrackingEventRepository trackingEventRepository;

    public FedexTrackingService(ShipmentRepository shipmentRepository, TrackingEventRepository trackingEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional
    public void parseTrackingResponseAndSave(ResponseEntity<String> response, HashMap<String , Long> trackNoMapOrderId) throws Exception {
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
                    TrackingEventEnum trackingEventEnum = FedexTrackingEnums.toTrackingEvent(derivedStatusCode);
                    if(trackingEventEnum == null){
                        continue;
                    }
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
                    //delivered 일경우 shipmenet deliveredAt save
                    if(trackingEventEnum.equals(TrackingEventEnum.DELIVERED)){
                        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                                .orElseThrow(()-> new ApiException("배송을 찾을 수 없습니다." , ErrorType.NOT_FOUND));
                        shipment.setDeliveredAt(scanEventAt);

                    }

                }
            }
            List<TrackingEvent> deduplicated = trackingEvents.stream()
                    .filter(event -> !trackingEventRepository.existsByOrderIdAndEvent(event.getOrderId(), event.getEvent()))
                    .toList();
            trackingEventRepository.saveAll(deduplicated);
        }
    }
}
