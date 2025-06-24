package org.example.oshipserver.domain.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentConfirmResponse {
    // record를 class로 리팩토링 ; record는 Jackson 역직렬화 잘 안 됨

    @JsonProperty("mid")
    private String mId;

    @JsonProperty("lastTransactionKey")
    private String lastTransactionKey;

    @JsonProperty("paymentKey")
    private String paymentKey;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("orderName")
    private String orderName;

    @JsonProperty("taxExemptionAmount")
    private int taxExemptionAmount;

    @JsonProperty("status")
    private String status;

    @JsonProperty("requestedAt")
    private String requestedAt;

    @JsonProperty("approvedAt")
    private String approvedAt;

    @JsonProperty("useEscrow")
    private boolean useEscrow;

    @JsonProperty("cultureExpense")
    private boolean cultureExpense;

    @JsonProperty("card")
    private Card card;

    @JsonProperty("type")
    private String type;

    @JsonProperty("easyPay")
    private EasyPay easyPay;

    @JsonProperty("country")
    private String country;

    @JsonProperty("failure")
    private Failure failure;

    @JsonProperty("partialCancelable")
    private boolean isPartialCancelable;

    @JsonProperty("receipt")
    private Receipt receipt;

    @JsonProperty("checkout")
    private Checkout checkout;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("totalAmount")
    private int totalAmount;

    @JsonProperty("balanceAmount")
    private int balanceAmount;

    @JsonProperty("suppliedAmount")
    private int suppliedAmount;

    @JsonProperty("vat")
    private int vat;

    @JsonProperty("taxFreeAmount")
    private int taxFreeAmount;

    @JsonProperty("method")
    private String method;

    @JsonProperty("version")
    private String version;

    @Getter
    @Setter
    public static class Card {

        @JsonProperty("issuerCode")
        private String issuerCode;

        @JsonProperty("acquirerCode")
        private String acquirerCode;

        @JsonProperty("number")
        private String number;

        @JsonProperty("installmentPlanMonths")
        private int installmentPlanMonths;

        @JsonProperty("isInterestFree")
        private boolean isInterestFree;

        @JsonProperty("approveNo")
        private String approveNo;

        @JsonProperty("useCardPoint")
        private boolean useCardPoint;

        @JsonProperty("cardType")
        private String cardType;

        @JsonProperty("ownerType")
        private String ownerType;

        @JsonProperty("acquireStatus")
        private String acquireStatus;

        @JsonProperty("amount")
        private int amount;
    }

    @Getter
    @Setter
    public static class EasyPay {

        @JsonProperty("provider")
        private String provider;

        @JsonProperty("amount")
        private int amount;

        @JsonProperty("discountAmount")
        private int discountAmount;
    }

    @Getter
    @Setter
    public static class Failure {

        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;
    }

    @Getter
    @Setter
    public static class Receipt {

        @JsonProperty("url")
        private String url;
    }

    @Getter
    @Setter
    public static class Checkout {

        @JsonProperty("url")
        private String url;
    }
}
