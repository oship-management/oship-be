package org.example.oshipserver.domain.order.repository.jdbc;

import java.util.List;
import java.util.Map;
import org.example.oshipserver.domain.order.dto.bulk.OrderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderItemBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderRecipientBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.OrderSenderBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.RecipientAddressBulkDto;
import org.example.oshipserver.domain.order.dto.bulk.SenderAddressBulkDto;

public interface IOrderJdbcRepository {
    void bulkInsertOrders(List<OrderBulkDto> orders);
    void bulkInsertOrderItems(List<OrderItemBulkDto> items);
    void bulkInsertOrderSenders(List<OrderSenderBulkDto> senders);
    void bulkInsertOrderRecipients(List<OrderRecipientBulkDto> recipients);
    void bulkInsertSenderAddresses(List<SenderAddressBulkDto> addresses);
    void bulkInsertRecipientAddresses(List<RecipientAddressBulkDto> addresses);
    Map<String, Long> findOrderIdMapByMasterNos(List<String> masterNos);

    Map<String, Long> findSenderAddressIdsByMasterNos(List<String> masterNos);
    Map<String, Long> findRecipientAddressIdsByMasterNos(List<String> masterNos);


}