package org.example.oshipserver.domain.partner.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.admin.dto.request.RateCreateRequest;
import org.example.oshipserver.domain.admin.dto.request.RateGroupRequest;
import org.example.oshipserver.domain.admin.dto.response.ResponseRateDto;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.auth.repository.AuthAddressRepository;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.carrier.dto.response.CarrierListResponse;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.carrier.service.PartnerCarrierService;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.common.excel.record.ExcelParseResult;
import org.example.oshipserver.global.common.response.BaseResponse;
import org.example.oshipserver.global.common.utils.PasswordEncoder;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAddressRepository authAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CarrierRepository carrierRepository;
    private final RateExcelProcessor rateExcelProcessor;
    private final PartnerCarrierService partnerCarrierService;

    @Transactional(readOnly = true)
    public PartnerInfoResponse getPartnerInfo(Long userId) {

        return partnerRepository.findPartnerInfoByUserId(userId)
                .orElseThrow(() -> new ApiException("파트너 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
    }
    @Transactional
    public void deletePartner(Long userId, PartnerDeleteRequest request, String accessToken){
        User findUser = userRepository.findById(userId)
                .orElseThrow(()->new ApiException("파트너 조회 실패", ErrorType.NOT_FOUND));
        if (!request.password().equals(request.passwordValid())) {
            throw new ApiException("비밀번호가 일치하지 않습니다", ErrorType.VALID_FAIL);
        }
        if (!passwordEncoder.matches(request.password(), findUser.getPassword())) {
            throw new ApiException("비밀번호가 틀렸습니다", ErrorType.VALID_FAIL);
        }
        refreshTokenRepository.addBlackList(accessToken);
        refreshTokenRepository.deleteRefreshToken(findUser.getId());
        findUser.softDelete();
    }

    @Transactional
    public AuthAddressResponse updateAddress(Long userId, AuthAddressRequest request){
        AuthAddress findAddress = authAddressRepository.findByUserId(userId)
                .orElseThrow(()->new ApiException("주소 정보를 찾을 수 없습니다", ErrorType.NOT_FOUND));
        findAddress.update(request);
        return AuthAddressResponse.from(findAddress);
    }

    @Transactional(readOnly = true)
    public List<CarrierListResponse> getPartnerCarriers(Long userId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException("파트너 정보를 찾을 수 없습니다.", ErrorType.NOT_FOUND));

        List<Carrier> carriers = carrierRepository.findByPartnerId(partner.getId());

        return carriers.stream()
                .map(CarrierListResponse::from)
                .collect(Collectors.toList());
    }

    public BaseResponse<ResponseRateDto> uploadRateExcel(MultipartFile file, String userId, Long carrierId) {

        Long partnerId = partnerRepository.findByUserId(Long.valueOf(userId))
            .orElseThrow(() -> new ApiException("해당 userId를 가진 partner가 없습니다.", ErrorType.INVALID_PARAMETER)).getId();

        partnerCarrierService.findCarrierOrThrow(partnerId, carrierId);

        ExcelParseResult<RateCreateRequest> records = rateExcelProcessor.process(file);

        boolean dup = hasDuplicates(records.success());
        if(dup){
            throw new ApiException("엑셀에 중복 weight가 있습니다.", ErrorType.INVALID_PARAMETER);
        }

        if (!records.errors().isEmpty()) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST.value(), "엑셀 파싱 실패",
                ResponseRateDto.from(records));
        }

        List<RateGroupRequest> grouped = records.success().stream()
            .flatMap(r -> r.amounts().entrySet().stream()
                .map(e -> Map.entry(
                    e.getKey(),
                    new RateGroupRequest.amounts(
                        r.weight(),
                        e.getValue()
                    )
                ))
            )
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
            ))
            .entrySet().stream()
            .map(e -> new RateGroupRequest(
                carrierId,
                e.getKey(),
                e.getValue()
            ))
            .toList();

        StringBuilder message = new StringBuilder();
        for (RateGroupRequest r : grouped) {
            boolean checked = partnerCarrierService.validateZone(r.zoneIndex(), r.carrierId());

            if (!checked) {
                message.append(r.zoneIndex()).append(" ");
            }
        }

        if (!message.isEmpty()) {
            message.append("에 해당하는 zone이 등록되지 않았습니다.");
            return new BaseResponse<>(HttpStatus.BAD_REQUEST.value(), message.toString(), null);
        }

        ResponseRateDto result = partnerCarrierService.createRate(grouped);

        if (result.errors().isEmpty()) {
            return new BaseResponse<>(HttpStatus.CREATED.value(), "성공", result);
        }
        return new BaseResponse<>(HttpStatus.BAD_REQUEST.value(), "엑셀 저장 실패", result);
    }

    private static boolean hasDuplicates(List<RateCreateRequest> list) {
        Set<BigDecimal> set = new HashSet<>();
        for (RateCreateRequest item : list) {
            if (!set.add(item.weight())) {
                return true;
            }
        }
        return false;
    }

}
