package org.example.oshipserver.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.request.LoginRequest;
import org.example.oshipserver.domain.auth.dto.request.PartnerSignupRequest;
import org.example.oshipserver.domain.auth.dto.request.SellerSignupRequest;
import org.example.oshipserver.domain.auth.enums.AuthErrorType;
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

@Transactional
public Long signupSeller(SellerSignupRequest request) {

    if (userRepository.existsByEmail(request.email())) {
        throw new ApiException("이미 존재하는 이메일입니다.", AuthErrorType.EMAIL_ALREADY_EXISTS);
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

    sellerRepository.save(seller);
    return savedUser.getId();
}

@Transactional
public Long signupPartner(PartnerSignupRequest request) {

    if (userRepository.existsByEmail(request.email())) {
        throw new ApiException("이미 존재하는 이메일입니다.", AuthErrorType.EMAIL_ALREADY_EXISTS);
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

    partnerRepository.save(partner);

    return savedUser.getId();
}

@Transactional(readOnly = true)
public String login(LoginRequest request) {

    User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new ApiException(AuthErrorType.USER_NOT_FOUND.getDesc(),
                        AuthErrorType.USER_NOT_FOUND));
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw new ApiException(AuthErrorType.PASSWORD_MISMATCH.getDesc(),
            AuthErrorType.PASSWORD_MISMATCH);
    }

    return jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());
}
}
