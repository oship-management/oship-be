package org.example.oshipserver.domain.partner.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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
                                user.lastLoginAt
                        ))
                        .from(partner)
                        .join(user).on(partner.userId.eq(user.id))
                        .where(user.id.eq(userId))
                        .fetchOne()
        );
    }
}

