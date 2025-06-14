package org.example.oshipserver.domain.partner.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.auth.entity.QAuthAddress;
import org.example.oshipserver.domain.partner.dto.response.PartnerInfoResponse;
import org.example.oshipserver.domain.partner.entity.QPartner;
import org.example.oshipserver.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PartnerRepositoryImpl implements IPartnerRepository{
    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Optional<PartnerInfoResponse> findPartnerInfoByUserId(Long userId) {
        QPartner partner = QPartner.partner;
        QUser user = QUser.user;
        QAuthAddress authAddress = QAuthAddress.authAddress;

        return Optional.ofNullable(
                jpaQueryFactory
                        .select(Projections.constructor(
                                PartnerInfoResponse.class,
                                partner.id,
                                partner.companyName,
                                partner.companyTelNo,
                                partner.companyRegisterNo,
                                user.id,
                                user.email,
                                user.userRole,
                                user.lastLoginAt,
                                Projections.constructor(
                                        AuthAddressResponse.class,
                                        authAddress.country,
                                        authAddress.city,
                                        authAddress.state,
                                        authAddress.detail1,
                                        authAddress.detail2,
                                        authAddress.zipCode
                                )
                        ))
                        .from(partner)
                        .join(user).on(partner.userId.eq(user.id))
                        .join(authAddress).on(partner.userId.eq(authAddress.userId))
                        .where(user.id.eq(userId))
                        .fetchOne()
        );
    }
}

