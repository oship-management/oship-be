package org.example.oshipserver.domain.partner.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.auth.repository.AuthAddressRepository;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.partner.dto.request.PartnerDeleteRequest;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.carrier.dto.response.CarrierListResponse;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.carrier.repository.CarrierRepository;
import org.example.oshipserver.domain.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.common.utils.PasswordEncoder;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAddressRepository authAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CarrierRepository carrierRepository;

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

}
