package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.dto.request.OrderUpdateRequest;

@Entity
@Table(name = "order_recipients")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientName;
    private String recipientCompany;
    private String recipientEmail;
    private String recipientPhoneNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "recipient_address_id")
    private RecipientAddress recipientAddress;

    public void updateFrom(OrderUpdateRequest req) {
        this.recipientName = req.recipientName();
        this.recipientCompany = req.recipientCompany();
        this.recipientEmail = req.recipientEmail();
        this.recipientPhoneNo = req.recipientPhoneNo();

        if (this.recipientAddress == null) {
            this.recipientAddress = RecipientAddress.builder()
                .recipientCountryCode(req.recipientCountryCode())
                .recipientState(req.recipientState())
                .recipientStateCode(req.recipientStateCode())
                .recipientCity(req.recipientCity())
                .recipientAddress1(req.recipientAddress1())
                .recipientAddress2(req.recipientAddress2())
                .recipientZipCode(req.recipientZipCode())
                .recipientTaxId(req.recipientTaxId())
                .build();
        } else {
            this.recipientAddress.updateFrom(req);
        }
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

}
