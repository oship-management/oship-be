package org.example.oshipserver.domain.seller.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.AuthAddressRequest;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.auth.repository.AuthAddressRepository;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.seller.dto.request.SellerDeleteRequest;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.common.utils.PasswordEncoder;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAddressRepository authAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public SellerInfoResponse getSellerInfo(Long userId){
        SellerInfoResponse response = sellerRepository.findSellerInfoByUserId(userId)
                .orElseThrow(()->new ApiException("셀러 조회 실패", ErrorType.NOT_FOUND));
        return response;
    }

    @Transactional
    public void deleteSeller(Long userId, SellerDeleteRequest request){
        System.out.println(request.password() + " " + request.passwordValid());
        User findUser = userRepository.findById(userId)
                .orElseThrow(()->new ApiException("셀러 조회 실패", ErrorType.NOT_FOUND));
        if(!findUser.getUserRole().equals(UserRole.SELLER)){
            throw new ApiException("셀러가 아닙니다", ErrorType.FAIL);
        }
        if (!request.password().equals(request.passwordValid())) {
            throw new ApiException("비밀번호가 일치하지 않습니다", ErrorType.VALID_FAIL);
        }
        if (!passwordEncoder.matches(request.password(), findUser.getPassword())) {
            throw new ApiException("비밀번호가 틀렸습니다", ErrorType.VALID_FAIL);
        }
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

}
