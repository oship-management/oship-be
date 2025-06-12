package org.example.oshipserver.client.fedex;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.example.oshipserver.domain.auth.entity.AuthAddress;
import org.example.oshipserver.domain.carrier.entity.Carrier;
import org.example.oshipserver.domain.order.entity.Order;
import org.example.oshipserver.domain.partner.entity.Partner;
import org.example.oshipserver.domain.shipping.dto.request.ShipmentMeasureRequest;

public record FedexShipmentRequest(
    String labelResponseOptions,
    RequestedShipment requestedShipment,
    AccountNumber accountNumber
) {
    public record RequestedShipment(
        Shipper shipper,
        List<Recipient> recipients,
        String shipDatestamp,
        String serviceType,
        String packagingType,
        String pickupType,
        Boolean blockInsightVisibility,
        ShippingChargesPayment shippingChargesPayment,
        LabelSpecification labelSpecification,
        CustomsClearanceDetail customsClearanceDetail,
        List<RequestedPackageLineItem> requestedPackageLineItems
    ) {}

    public record Shipper(
        Contact contact,
        ShipperAddress address
    ) {}

    public record Recipient(
        Contact contact,
        RecipientAddress address
    ) {}

    public record Contact(
        String personName,
        String phoneNumber
    ) {}

    public record ShipperAddress(
        List<String> streetLines,
        String city,
        String postalCode,
        String countryCode
    ) {}

    public record RecipientAddress(
        List<String> streetLines,
        String city,
        String stateOrProvinceCode,
        String postalCode,
        String countryCode
    ) {}

    public record ShippingChargesPayment(
        String paymentType
    ) {}

    public record LabelSpecification(
        String imageType,
        String labelStockType
    ) {}

    public record CustomsClearanceDetail(
        DutiesPayment dutiesPayment,
        Boolean isDocumentOnly,
        CommercialInvoice commercialInvoice,
        List<Commodity> commodities
    ) {}

    public record DutiesPayment(
        String paymentType
    ) {}

    public record CommercialInvoice(
        String termsOfSale
    ) {}

    public record Commodity(
        String description,
        String countryOfManufacture,
        int quantity,
        String quantityUnits,
        Money unitPrice,
        Money customsValue,
        Weight weight
    ) {}

    public record Money(
        BigDecimal amount,
        String currency
    ) {}

    public record Weight(
        String units,
        BigDecimal value
    ) {}

    public record RequestedPackageLineItem(
        Weight weight,
        Dimensions dimensions,
        int groupPackageCount,
        int sequenceNumber,
        List<CustomerReference> customerReferences
    ) {}

    public record Dimensions(
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        String units
    ) {}

    public record CustomerReference(
        String customerReferenceType,
        String value
    ) {}

    public record AccountNumber(
        String value
    ) {}

    public static FedexShipmentRequest from(
        Order order,
        ShipmentMeasureRequest shipmentMeasureRequest,
        Carrier carrier,
        AuthAddress authAddress,
        Partner partner
    ) {

        String countryCode = String.valueOf(
            order.getRecipient().getRecipientAddress().getRecipientCountryCode());

        String stateOrProvinceCode = order.getRecipient().getRecipientAddress().getRecipientStateCode().name();

        Set<String> countryWithState = Set.of("CA", "US", "IN", "MX", "AE");
        String finalStateCode = countryWithState.contains(countryCode) ? stateOrProvinceCode : order.getRecipient().getRecipientAddress().getRecipientState();

        return new FedexShipmentRequest(
            "URL_ONLY",
            new RequestedShipment(
                new Shipper(
                    new Contact(partner.getCompanyName(), partner.getCompanyTelNo()),
                    new ShipperAddress(
                        List.of(authAddress.getDetail1(), authAddress.getDetail2()),
                        authAddress.getCity(),
                        authAddress.getZipCode(),
                        authAddress.getCountry()
                    )
                ),
                List.of(
                    new Recipient(
                        new Contact(
                            order.getRecipient().getRecipientName(),
                            order.getRecipient().getRecipientPhoneNo()
                        ),
                        new RecipientAddress(
                            List.of(
                                order.getRecipient().getRecipientAddress().getRecipientAddress1(),
                                order.getRecipient().getRecipientAddress().getRecipientAddress2()
                            ),
                            order.getRecipient().getRecipientAddress().getRecipientCity(),
                            finalStateCode, // "CA", "US", "IN", "MX", "AE" 특정국가는 StateCode로
                            order.getRecipient().getRecipientAddress().getRecipientZipCode(),
                            countryCode // 미리 변수 선언
                        )
                    )
                ),
                LocalDate.now().toString(),
                carrier.getService().getDesc(),
                "YOUR_PACKAGING",
                "CONTACT_FEDEX_TO_SCHEDULE",
                false, // blockInsightVisibility
                new ShippingChargesPayment("SENDER"),
                new LabelSpecification("PNG", "PAPER_4X6"),
                new CustomsClearanceDetail(
                    new DutiesPayment("SENDER"),
                    false,
                    new CommercialInvoice("CFR"),
                    order.getOrderItems().stream()
                        .map(item -> new Commodity(
                            item.getName(),
                            item.getOriginCountryCode(),
                            item.getQuantity(),
                            "EA",
                            new Money(item.getUnitValue(), "WON"),
                            new Money(item.getUnitValue().multiply(BigDecimal.valueOf(item.getQuantity())), "WON"),
                            new Weight(item.getWeightUnit(), item.getWeight())
                        ))
                        .toList()
                ),
                List.of(
                    new RequestedPackageLineItem(
                        new Weight("KG", shipmentMeasureRequest.grossWeight()),
                        new Dimensions(
                            shipmentMeasureRequest.length(),
                            shipmentMeasureRequest.width(),
                            shipmentMeasureRequest.height(),
                            "CM"
                        ),
                        1, //groupPackageCount 똑같은 박스 여러개일 때, 박스갯수 MVP라서 1 고정
                        1, //sequenceNumber 박스가 여러개일때 순서번호 MVP라서 1 고정
                        List.of(
                            new CustomerReference(
                                "CUSTOMER_REFERENCE",
                                order.getOshipMasterNo()
                            )
                        )
                    )
                )
            ),
            new AccountNumber(carrier.getAccountNumber())
        );
    }
}
