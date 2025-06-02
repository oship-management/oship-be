package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_senders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long sellerId;

    @Column(name = "store_platform")
    private String storePlatform;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "name") 
    private String senderName;

    @Column(name = "email")
    private String senderEmail;

    @Column(name = "phone_no")
    private String senderPhoneNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_address_id")
    private SenderAddress address;
}
