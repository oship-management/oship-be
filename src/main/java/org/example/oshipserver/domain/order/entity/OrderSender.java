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
@Table(name = "order_senders")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class OrderSender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderName;
    private String senderCompany;
    private String senderEmail;
    private String senderPhoneNo;

    private String storePlatform;
    private String storeName;

    private Long sellerId;  // 추후 seller 연관관계 대체 예정

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_address_id")
    private SenderAddress senderAddress;

    public void assignOrder(Order order) {
        this.order = order;
    }

    public void updateFrom(OrderUpdateRequest req) {
        this.senderName = req.senderName();
        this.senderCompany = req.senderCompany();
        this.senderEmail = req.senderEmail();
        this.senderPhoneNo = req.senderPhoneNo();
        this.storePlatform = req.storePlatform();
        this.storeName = req.storeName();
        this.sellerId = req.sellerId();

        if (this.senderAddress == null) {
            this.senderAddress = SenderAddress.builder()
                .senderCountryCode(req.senderCountryCode())
                .senderState(req.senderState())
                .senderStateCode(req.senderStateCode())
                .senderCity(req.senderCity())
                .senderAddress1(req.senderAddress1())
                .senderAddress2(req.senderAddress2())
                .senderZipCode(req.senderZipCode())
                .senderTaxId(req.senderTaxId())
                .build();
        } else {
            this.senderAddress.updateFrom(req);
        }
    }
}
