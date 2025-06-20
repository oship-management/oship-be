package org.example.oshipserver.domain.order.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.order.entity.QOrder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IOrderRepositoryImpl implements IOrderRepository {

    private final JPAQueryFactory queryFactory;

    private final QOrder order = QOrder.order;

    @Override
    public List<String> findExistingOrderNos(Long sellerId, List<String> orderNos) {
        return queryFactory
            .select(order.orderNo)
            .from(order)
            .where(order.sellerId.eq(sellerId)
                .and(order.orderNo.in(orderNos)))
            .fetch();
    }

    @Override
    public List<String> findExistingMasterNos(List<String> masterNos) {
        return queryFactory
            .select(order.oshipMasterNo)
            .from(order)
            .where(order.oshipMasterNo.in(masterNos))
            .fetch();
    }

}
