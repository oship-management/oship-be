package org.example.oshipserver.domain.order.repository;

import java.util.List;

public interface IOrderRepository {
    List<String> findExistingOrderNos(Long sellerId, List<String> orderNos);

    List<String> findExistingMasterNos(List<String> masterNos);

}
