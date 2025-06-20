package org.example.oshipserver.domain.order.dto.request;

import static org.example.oshipserver.domain.order.util.ExcelCellUtils.getDouble;
import static org.example.oshipserver.domain.order.util.ExcelCellUtils.getInt;
import static org.example.oshipserver.domain.order.util.ExcelCellUtils.getString;

import org.apache.poi.ss.usermodel.Row;

public record OrderExcelRequest(
    String storePlatform,
    String storeName,
    String orderNo,
    String shippingTerm,
    String senderName,
    String senderCompany,
    String senderEmail,
    String senderPhoneNo,
    String senderAddress1,
    String senderAddress2,
    String senderCity,
    String senderState,
    String senderZipCode,
    String senderCountryCode,
    String senderTaxId,
    String recipientName,
    String recipientCompany,
    String recipientEmail,
    String recipientPhoneNo,
    String recipientTaxId,
    String recipientAddress1,
    String recipientAddress2,
    String recipientCity,
    String recipientState,
    String recipientZipCode,
    String recipientCountryCode,
    Double shipmentActualWeight,
    Double shipmentVolumeWeight,
    String weightUnit,
    Double dimensionWidth,
    Double dimensionLength,
    Double dimensionHeight,
    String packageType,
    Integer parcelCount,
    String serviceType,
    String itemContentsType,
    String itemName,
    Integer itemQuantity,
    Double itemUnitValue,
    String itemValueCurrency,
    Double itemWeight,
    String itemHSCode,
    String itemOriginCountryCode,
    String lastTrackingEvent
) {
    public static OrderExcelRequest from(Row row) {
        return new OrderExcelRequest(
            getString(row, 0), getString(row, 1), getString(row, 2), getString(row, 3),
            getString(row, 4), getString(row, 5), getString(row, 6), getString(row, 7), getString(row, 8),
            getString(row, 9), getString(row, 10), getString(row, 11), getString(row, 12), getString(row, 13),
            getString(row, 14), getString(row, 15), getString(row, 16), getString(row, 17), getString(row, 18),
            getString(row, 19), getString(row, 20), getString(row, 21), getString(row, 22), getString(row, 23),
            getString(row, 24), getString(row, 25), getDouble(row, 26), getDouble(row, 27), getString(row, 28),
            getDouble(row, 29), getDouble(row, 30), getDouble(row, 31), getString(row, 32), getInt(row, 33),
            getString(row, 34), getString(row, 35), getString(row, 36), getInt(row, 37), getDouble(row, 38),
            getString(row, 39), getDouble(row, 40), getString(row, 41), getString(row, 42),getString(row, 43)

        );
    }

}