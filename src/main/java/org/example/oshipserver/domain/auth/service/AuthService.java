package org.example.oshipserver.domain.auth.service;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.dto.response.TokenResponse;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.auth.repository.AuthAddressRepository;
import org.example.oshipserver.domain.auth.repository.RefreshTokenRepository;
import org.example.oshipserver.domain.auth.vo.RefreshTokenVo;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.partner.repository.PartnerRepository;
import org.example.oshipserver.domain.seller.entity.Seller;
import org.example.oshipserver.domain.seller.repository.SellerRepository;
import org.example.oshipserver.domain.user.entity.User;
import org.example.oshipserver.domain.user.enums.UserRole;
import org.example.oshipserver.domain.user.repository.UserRepository;
import org.example.oshipserver.global.common.utils.JwtUtil;
import org.example.oshipserver.global.common.utils.PasswordEncoder;
import org.example.oshipserver.global.exception.ApiException;
import org.example.oshipserver.global.exception.ErrorType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SellerRepository sellerRepository;
    private final PartnerRepository partnerRepository;
    private final AuthAddressRepository authAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    @Transactional
    public Long signupSeller(SellerSignupRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("이미 존재하는 이메일입니다.", ErrorType.FAIL);
        }
        if (sellerRepository.existsByCompanyRegisterNo(request.companyRegisterNo())) {
            throw new ApiException("이미 등록된 사업자번호입니다.", ErrorType.VALID_FAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        UserRole userRole = UserRole.of(request.userRole());

        User newUser = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .userRole(userRole)
                .build();
        User savedUser = userRepository.save(newUser);

        // 셀러 정보 저장
        Seller seller = Seller.builder()
                .userId(savedUser.getId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNo(request.phoneNo())
                .companyName(request.companyName())
                .companyRegisterNo(request.companyRegisterNo())
                .companyTelNo(request.companyTelNo())
                .build();
        AuthAddress address = AuthAddress.from(request.address(), savedUser.getId());
        sellerRepository.save(seller);
        authAddressRepository.save(address);
        return savedUser.getId();
    }

    @Transactional
    public Long signupPartner(PartnerSignupRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException("이미 존재하는 이메일입니다.", ErrorType.FAIL);
        }
        if (partnerRepository.existsByCompanyRegisterNo(request.companyRegisterNo())) {
            throw new ApiException("이미 등록된 사업자번호입니다.", ErrorType.VALID_FAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        UserRole userRole = UserRole.of(request.userRole());

        User newUser = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .userRole(userRole)
                .build();
        User savedUser = userRepository.save(newUser);

        // 파트너 정보 저장
        Partner partner = Partner.builder()
                .userId(savedUser.getId())
                .companyName(request.companyName())
                .companyRegisterNo(request.companyRegisterNo())
                .companyTelNo(request.companyTelNo())
                .build();
        AuthAddress address = AuthAddress.from(request.address(), savedUser.getId());
        partnerRepository.save(partner);
        authAddressRepository.save(address);

        return savedUser.getId();
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException("유저를 찾을 수 없습니다", ErrorType.NOT_FOUND));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException("비밀번호가 일치하지 않습니다.", ErrorType.FAIL);
        }
        user.setLastLoginAt();
        String accessToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
        RefreshTokenVo refreshToken = jwtUtil.createRefreshToken(user.getId());
        //이 리프레쉬토큰을 레디스에 저장해야댐
        refreshTokenRepository.saveRefreshToken(
                user.getId(),
                refreshToken.getRefreshToken(),
                refreshToken.getExpiredAt().getTime() - System.currentTimeMillis()
        );
        return new TokenResponse(accessToken);
    }

    //로그아웃 리프레시토큰 레디스에서 삭제
    @Transactional
    public void logout(Long userId, String accessToken) {
        refreshTokenRepository.addBlackList(accessToken);
        refreshTokenRepository.deleteRefreshToken(userId);
    }

    //토큰을 발급하는 함수
    @Transactional(readOnly = true)
    public TokenResponse refreshToken(HttpServletRequest request) {
        String jwt = jwtUtil.extractTokenFromHeader(request);
        jwt = jwt.substring(7);
        Claims claims = jwtUtil.getClaims(jwt);
        Long userId = Long.valueOf(claims.getSubject());
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("유저를 찾을 수 없습니다.", ErrorType.NOT_FOUND));
        String refreshToken = refreshTokenRepository.getRefreshToken(userId);
        if(!jwtUtil.isRefreshTokenValid(refreshToken)){
            throw new ApiException(ErrorType.TOKEN_EXPIRED.getDesc(), ErrorType.TOKEN_EXPIRED);
        }
        String accessToken = jwtUtil.createToken(findUser.getId(), findUser.getEmail(), findUser.getUserRole());
        return new TokenResponse(accessToken);
    }

}
