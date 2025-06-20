package org.example.oshipserver.domain.order.repository.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oshipserver.domain.order.dto.bulk.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class IOrderJdbcRepositoryImpl implements IOrderJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Map<String, Long> findOrderIdMapByMasterNos(List<String> masterNos) {
        if (masterNos == null || masterNos.isEmpty()) return Map.of();

        String sql = "SELECT oship_master_no, id FROM orders WHERE oship_master_no IN (:masterNos)";
        MapSqlParameterSource parameters = new MapSqlParameterSource("masterNos", masterNos);

        return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("oship_master_no"), rs.getLong("id"));
            }
            return result;
        });
    }

    @Override
    public void bulkInsertOrders(List<OrderBulkDto> orders) {
        String sql = "INSERT INTO orders (order_no, oship_master_no, shipping_term, service_type, weight_unit, " +
            "shipment_actual_weight, shipment_volume_weight, dimension_width, dimension_length, dimension_height, " +
            "package_type, parcel_count, item_contents_type, deleted, created_at, modified_at, seller_id,last_tracking_event) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderBulkDto o = orders.get(i);
                log.info("[ORDER INSERT] [{}] orderNo={}, deleted={}, createdAt={}, modifiedAt={}",
                    i, o.orderNo(), o.deleted(), o.createdAt(), o.modifiedAt());

                ps.setString(1, o.orderNo());
                ps.setString(2, o.oshipMasterNo());
                ps.setString(3, o.shippingTerm());
                ps.setString(4, o.serviceType());
                ps.setString(5, o.weightUnit());
                setDoubleOrNull(ps, 6, o.shipmentActualWeight());
                setDoubleOrNull(ps, 7, o.shipmentVolumeWeight());
                setDoubleOrNull(ps, 8, o.dimensionWidth());
                setDoubleOrNull(ps, 9, o.dimensionLength());
                setDoubleOrNull(ps, 10, o.dimensionHeight());
                ps.setString(11, o.packageType());
                setIntOrNull(ps, 12, o.parcelCount());
                ps.setString(13, o.itemContentsType());
                ps.setByte(14, (byte) (Boolean.TRUE.equals(o.deleted()) ? 1 : 0));
                ps.setTimestamp(15, toTimestamp(o.createdAt()));
                ps.setTimestamp(16, toTimestamp(o.modifiedAt()));
                ps.setObject(17, o.sellerId());
                ps.setString(18, o.lastTrackingEvent());
            }

            @Override
            public int getBatchSize() {
                return orders.size();
            }
        });
    }

    private Timestamp toTimestamp(LocalDateTime time) {
        return (time != null)
            ? Timestamp.valueOf(time.withNano((time.getNano() / 1_000_000) * 1_000_000))
            : new Timestamp(System.currentTimeMillis());
    }

    private void setDoubleOrNull(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value != null) ps.setDouble(index, value);
        else ps.setNull(index, java.sql.Types.DOUBLE);
    }

    private void setIntOrNull(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) ps.setInt(index, value);
        else ps.setNull(index, java.sql.Types.INTEGER);
    }

    private void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) ps.setLong(index, value);
        else ps.setNull(index, java.sql.Types.BIGINT);
    }

    @Override
    public Map<String, Long> findSenderAddressIdsByMasterNos(List<String> masterNos) {
        if (masterNos == null || masterNos.isEmpty()) return Map.of();

        String sql = "SELECT id FROM sender_addresses ORDER BY id DESC LIMIT :size";
        MapSqlParameterSource parameters = new MapSqlParameterSource("size", masterNos.size());

        return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Map<String, Long> result = new HashMap<>();
            int index = 0;
            while (rs.next() && index < masterNos.size()) {
                result.put(masterNos.get(index++), rs.getLong("id"));
            }
            return result;
        });
    }

    @Override
    public Map<String, Long> findRecipientAddressIdsByMasterNos(List<String> masterNos) {
        if (masterNos == null || masterNos.isEmpty()) return Map.of();

        String sql = "SELECT id FROM recipient_addresses ORDER BY id DESC LIMIT :size";
        MapSqlParameterSource parameters = new MapSqlParameterSource("size", masterNos.size());

        return namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Map<String, Long> result = new HashMap<>();
            int index = 0;
            while (rs.next() && index < masterNos.size()) {
                result.put(masterNos.get(index++), rs.getLong("id"));
            }
            return result;
        });
    }

    @Override
    public void bulkInsertOrderItems(List<OrderItemBulkDto> items) {
        String sql = "INSERT INTO order_items (" +
            "name, quantity, unit_value, value_currency, " +
            "weight, hs_code, origin_country_code, item_origin_state_code, item_origin_state_name, weight_unit, " +
            "order_id, created_at, modified_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, items, 1000, (ps, item) -> {
            ps.setString(1, item.name());
            setIntOrNull(ps, 2, item.quantity());
            setDoubleOrNull(ps, 3, item.unitValue());
            ps.setString(4, item.valueCurrency());
            setDoubleOrNull(ps, 5, item.weight());
            ps.setString(6, item.hsCode());
            ps.setString(7, item.originCountryCode());
            ps.setString(8, item.originStateCode());
            ps.setString(9, item.originStateName());
            ps.setString(10, item.weightUnit());
            setLongOrNull(ps, 11, item.orderId());
            ps.setTimestamp(12, toTimestamp(item.createdAt()));
            ps.setTimestamp(13, toTimestamp(item.modifiedAt()));
        });
    }


    @Override
    public void bulkInsertOrderSenders(List<OrderSenderBulkDto> senders) {
        String sql = """
            INSERT INTO order_senders (
                order_id, seller_id, sender_address_id,
                sender_company, sender_email, sender_name, sender_phone_no,
                store_name, store_platform
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, senders, 1000, (ps, s) -> {
            ps.setObject(1, s.orderId());
            ps.setObject(2, s.sellerId());
            ps.setObject(3, s.senderAddressId());
            ps.setString(4, s.senderCompany());
            ps.setString(5, s.senderEmail());
            ps.setString(6, s.senderName());
            ps.setString(7, s.senderPhoneNo());
            ps.setString(8, s.storeName());
            ps.setString(9, s.storePlatform());
        });
    }

    @Override
    public void bulkInsertOrderRecipients(List<OrderRecipientBulkDto> recipients) {
        String sql = """
            INSERT INTO order_recipients (
                order_id,
                recipient_address_id,
                recipient_company,
                recipient_email,
                recipient_name,
                recipient_phone_no
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, recipients, 1000, (ps, r) -> {
            ps.setObject(1, r.orderId());
            ps.setObject(2, r.recipientAddressId());
            ps.setString(3, r.recipientCompany());
            ps.setString(4, r.recipientEmail());
            ps.setString(5, r.recipientName());
            ps.setString(6, r.recipientPhoneNo());
        });
    }

    @Override
    public void bulkInsertSenderAddresses(List<SenderAddressBulkDto> addresses) {
        String sql = """
            INSERT INTO sender_addresses (
                sender_address_1, sender_address_2, sender_city, sender_state,
                sender_state_code, sender_tax_id, sender_zip_code, sender_country_code
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, addresses, 1000, (ps, addr) -> {
            ps.setString(1, addr.senderAddress1());
            ps.setString(2, addr.senderAddress2());
            ps.setString(3, addr.senderCity());
            ps.setString(4, addr.senderState());
            ps.setString(5, addr.senderStateCode());
            ps.setString(6, addr.senderTaxId());
            ps.setString(7, addr.senderZipCode());
            ps.setString(8, addr.senderCountryCode());
        });
    }

    @Override
    public void bulkInsertRecipientAddresses(List<RecipientAddressBulkDto> addresses) {
        String sql = """
            INSERT INTO recipient_addresses (
                recipient_address_1, recipient_address_2, recipient_city, recipient_state,
                recipient_state_code, recipient_tax_id, recipient_zip_code, recipient_country_code
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, addresses, 1000, (ps, addr) -> {
            ps.setString(1, addr.recipientAddress1());
            ps.setString(2, addr.recipientAddress2());
            ps.setString(3, addr.recipientCity());
            ps.setString(4, addr.recipientState());
            ps.setString(5, addr.recipientStateCode());
            ps.setString(6, addr.recipientTaxId());
            ps.setString(7, addr.recipientZipCode());
            ps.setString(8, addr.recipientCountryCode());
        });
    }

}
