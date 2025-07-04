package org.example.oshipserver.domain.partner.service;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateGroupRequest;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.carrier.service.PartnerCarrierService;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult.ErrorDetail;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class PartnerUploadRateExcelTest {

    @Mock
    PartnerRepository partnerRepository;

    @Mock
    PartnerCarrierService partnerCarrierService;

    @Mock
    RateExcelProcessor  rateExcelProcessor;

    @InjectMocks
    PartnerService partnerService;
    
    private MockMultipartFile dummyFile;
    private String userId;
    private Long carrierId;

    @BeforeEach
    void setUp() {
        dummyFile = new MockMultipartFile(
            "file", "rates.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[]{1,2,3}
        );
        userId = "1";
        carrierId = 10L;
        // 테스트용 임의 ID
        Partner partner = Partner.builder()
            .id(42L)
            .userId(1L)
            .build();
        given(partnerRepository.findByUserId(anyLong()))
            .willReturn(Optional.of(partner));
    }

    @Test @DisplayName("파싱 오류 있으면 400 + 에러 내용 반환")
    void givenParseErrors_whenUpload_thenBadRequest() {
        // given
        List<ErrorDetail> errors = List.of(new ErrorDetail(2, "null 값입니다."));
        ExcelParseResult<RateCreateRequest> records = new ExcelParseResult<>(List.<RateCreateRequest>of(), errors);
        given(rateExcelProcessor.process(dummyFile)).willReturn(records);

        // when
        BaseResponse<ResponseRateDto> response = partnerService.uploadRateExcel(dummyFile, userId, carrierId);

        // then
        then(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        then(response.getMessage()).isEqualTo("엑셀 파싱 실패");
        then(response.getData().errors()).hasSize(1);
        then(response.getData().errors().get(0).rowIndex()).isEqualTo(2);
        verify(rateExcelProcessor, times(1)).process(any());
    }

    @Test @DisplayName("등록되지 않은 zone 있으면 400 + zone 번호 메시지")
    void givenInvalidZone_whenUpload_thenBadRequest() {
        // given
        BigDecimal weight = BigDecimal.valueOf(3);
        BigDecimal amount = BigDecimal.valueOf(15000);

        given(partnerCarrierService.validateZone(1, carrierId)).willReturn(false);
        Map<Integer, BigDecimal> amounts = Map.of(1, amount);
        RateCreateRequest rate = new RateCreateRequest(weight, amounts);
        ExcelParseResult<RateCreateRequest> records = new ExcelParseResult<>(List.of(rate), List.<ErrorDetail>of());
        given(rateExcelProcessor.process(dummyFile)).willReturn(records);

        // when
        BaseResponse<ResponseRateDto> response = partnerService.uploadRateExcel(dummyFile, userId, carrierId);

        // then
        then(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        then(response.getMessage()).isEqualTo("1 에 해당하는 zone이 등록되지 않았습니다.");
        verify(rateExcelProcessor, times(1)).process(any());
        verify(partnerCarrierService, times(1)).validateZone(anyInt(), anyLong());
    }

    @Test @DisplayName("정상 처리되면 201 + 결과 DTO 반환")
    void givenAllValid_whenUpload_thenCreated() {
        // given
        BigDecimal weight = BigDecimal.valueOf(3);
        BigDecimal amount = BigDecimal.valueOf(15000);
        int zone = 1;

        Map<Integer, BigDecimal> amounts = Map.of(zone, amount);
        RateCreateRequest rate = new RateCreateRequest(weight, amounts);
        ExcelParseResult<RateCreateRequest> records = new ExcelParseResult<>(List.of(rate), List.<ErrorDetail>of());
        given(rateExcelProcessor.process(dummyFile)).willReturn(records);
        given(partnerCarrierService.validateZone(zone, carrierId)).willReturn(true);

        List<RateGroupRequest> grouped = List.of(
            new RateGroupRequest(
                carrierId,
                zone,
                List.of(new RateGroupRequest.amounts(weight, amount))
            )
        );
        ResponseRateDto result = new ResponseRateDto(1, 0, List.of());
        given(partnerCarrierService.createRate(grouped)).willReturn(result);

        // when
        BaseResponse<ResponseRateDto> response = partnerService.uploadRateExcel(dummyFile, userId, carrierId);

        // then
        then(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        then(response.getMessage()).isEqualTo("성공");
        verify(rateExcelProcessor, times(1)).process(any());
        verify(partnerCarrierService, times(1)).validateZone(anyInt(), anyLong());
        verify(partnerCarrierService, times(1)).createRate(any());
    }

}
