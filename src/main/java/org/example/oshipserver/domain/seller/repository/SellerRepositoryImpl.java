package org.example.oshipserver.domain.seller.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.oshipserver.domain.auth.dto.response.AuthAddressResponse;
import org.example.oshipserver.domain.auth.entity.QAuthAddress;
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
        QAuthAddress authAddress = QAuthAddress.authAddress; //정보조회에 주소정보 끼워넣기
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
                .from(seller)
                .join(user).on(seller.userId.eq(user.id))
                .join(authAddress).on(seller.userId.eq(authAddress.userId))
                .where(seller.userId.eq(userId))
                .fetchOne()
        );
    }
}
