package org.example.oshipserver.domain.seller.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.seller.dto.response.SellerInfoResponse;
import org.example.oshipserver.domain.seller.entity.QSeller;
import org.example.oshipserver.domain.user.entity.QUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class SellerRepositoryImpl implements ISellerRepository{
    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public Optional<SellerInfoResponse> findSellerInfoByUserId(Long userId) {
        QSeller seller = QSeller.seller;
        QUser user = QUser.user;
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.constructor(
                        SellerInfoResponse.class,
                        seller.id,
                        seller.firstName,
                        seller.lastName,
                        seller.phoneNo,
                        seller.companyName,
                        seller.companyRegisterNo,
                        seller.companyTelNo,
                        user.id,
                        user.email,
                        user.userRole,
                        user.lastLoginAt
                ))
                .from(seller)
                .join(user).on(seller.userId.eq(user.id))
                .where(seller.userId.eq(userId))
                .fetchOne()
        );
    }
}
