package org.example.oshipserver.client.fedex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.client.fedex.dto.FedexTrackingRequest;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.auth.repository.AuthAddressRepository;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.shipping.dto.request.ShipmentMeasureRequest;
import org.example.oshipserver.domain.shipping.entity.Shipment;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class FedexClient { // 외부 연동 모듈

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CarrierRepository carrierRepository;
    private final AuthAddressRepository authAddressRepository;

    @Value("${fedex.api.url}")
    private String apiUrl;

    // 요청 url 생성
    private static final String FEDEX_TOKEN_URL_SUFFIX = "/oauth/token";
    private static final String FEDEX_SHIPMENT_URL_SUFFIX = "/ship/v1/shipments";
    private static final String FEDEX_TRACKING_URL_SUFFIX = "/track/v1/trackingnumbers";
    /**
     * FedEx 토큰 발급 요청
     */
    public String requestAccessToken(Long carrierId) {
        // Carrier 조회
        Carrier carrier = carrierRepository.findById(carrierId)
            .orElseThrow(() -> new ApiException("운송사 정보를 찾을 수 없습니다: " + carrierId, ErrorType.NOT_FOUND));

        String url = apiUrl + FEDEX_TOKEN_URL_SUFFIX;

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 바디 생성
        String body = "grant_type=client_credentials"
            + "&client_id=" + carrier.getApiKey()
            + "&client_secret=" + carrier.getSecretKey();

        // POST 요청을 위한 HttpEntity 생성
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            // RestTemplate을 통해 FedEx API에 POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            // 토큰 추출 및 저장
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();
            carrier.updateToken(accessToken);

            return accessToken;

        } catch (Exception e) {
            throw new ApiException("FedEx 토큰 발급 실패: " + e.getMessage(), ErrorType.EXTERNAL_SERVER_ERROR);
        }
    }

    /**
     * FedEx AWB 라벨 URL 요청
     */
    public FedexShipmentResponse requestAwbLabelUrl(
        Shipment shipment,
        Order order,
        ShipmentMeasureRequest shipmentMeasureRequest
    ) {
        // 1. Carrier 조회 및 토큰 확인
        Carrier carrier = carrierRepository.findById(shipment.getCarrierId())
            .orElseThrow(() -> new ApiException("운송사를 찾을 수 없습니다: " + shipment.getCarrierId(), ErrorType.NOT_FOUND));

        // 토큰이 없으면 새로 발급
        if (carrier.getToken() == null || carrier.getToken().isBlank()) {
            requestAccessToken(carrier.getId());
        }

        // 2. 출고지 주소 정보 조회
        AuthAddress authAddress = getAuthAddress(carrier);

        // 3. 요청 바디 생성
        FedexShipmentRequest fedexShipmentRequest = FedexShipmentRequest.from(
            order,
            shipmentMeasureRequest,
            carrier,
            authAddress,
            carrier.getPartner()
        );

        String url = apiUrl + FEDEX_SHIPMENT_URL_SUFFIX;

        try {
            // JSON 변환
            String jsonBody = objectMapper.writeValueAsString(fedexShipmentRequest);

            // 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(carrier.getToken());
            headers.set("x-locale", "en_KR");

            // POST 요청을 위한 HttpEntity 생성
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // RestTemplate을 통해 FedEx API에 POST 요청
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            // 401 응답시 토큰 재발급 후 재시도
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                String newToken = requestAccessToken(carrier.getId());
                headers.setBearerAuth(newToken);
                entity = new HttpEntity<>(jsonBody, headers);

                response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
                );
            }

            // 400 응답시 FedEx 에러 파싱
            if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ApiException(
                    response.getBody() + response.getStatusCode(),
                    ErrorType.FEDEX_BAD_REQUEST
                );
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            // 라벨 URL 추출
            JsonNode documents = root.at("/output/transactionShipments/0/pieceResponses/0/packageDocuments");
            String url1 = documents.get(0).get("url").asText();
            String url2 = documents.get(1).get("url").asText();
            String labelUrl = url1 + "," + url2;

            // 트래킹 번호 및 배송일 추출
            String carrierTrackingNo = root.at("/output/transactionShipments/0/masterTrackingNumber").asText();
            String shipDate = root.at("/output/transactionShipments/0/shipDatestamp").asText();

            // 응답 레코드 생성 및 반환
            return FedexShipmentResponse.builder()
                .carrierTrackingNo(carrierTrackingNo)
                .labelUrl(labelUrl)
                .shipDate(shipDate)
                .build();

        } catch (Exception e) {
            throw new ApiException("FedEx ERROR: " + e.getMessage(), ErrorType.FEDEX_BAD_REQUEST);
        }
    }

    /**
     * 출고지 주소 정보 조회
     */
    private AuthAddress getAuthAddress(Carrier carrier) {
        Partner partner = carrier.getPartner();
        if (partner == null) {
            throw new ApiException("운송사의 파트너 정보가 없습니다.", ErrorType.NOT_FOUND);
        }

        Long userId = partner.getUserId();
        if (userId == null) {
            throw new ApiException("파트너에 user_id가 없습니다.", ErrorType.NOT_FOUND);
        }

        return authAddressRepository.findByUserId(userId)
            .orElseThrow(() -> new ApiException("출고지 주소를 찾을 수 없습니다: userId=" + userId, ErrorType.NOT_FOUND));
    }

    protected ResponseEntity<String> tracking(String token, FedexTrackingRequest fedexTrackingRequest){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // 발급받은 FedEx OAuth 토큰

        HttpEntity<FedexTrackingRequest> requestEntity = new HttpEntity<>(fedexTrackingRequest, headers);

        ResponseEntity<String> response =restTemplate.postForEntity(
                apiUrl+FEDEX_TRACKING_URL_SUFFIX,
                requestEntity,
                String.class
        );
        log.info("fedex tracking info {}", response); //응답오는거 받아야함
        return response;
    }


}