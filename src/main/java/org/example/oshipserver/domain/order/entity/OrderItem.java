package org.example.oshipserver.domain.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.oshipserver.domain.order.dto.OrderItemDto;
import org.example.oshipserver.domain.order.dto.request.OrderItemRequest;
import org.example.oshipserver.global.entity.BaseTimeEntity;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer quantity;
    private BigDecimal unitValue;
    private String valueCurrency;
    private String weightUnit;
    private BigDecimal weight;
    private String hsCode;
    private String originCountryCode;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder
    private OrderItem(
        String name, int quantity, BigDecimal unitValue, String valueCurrency,
        BigDecimal weight, String weightUnit, String hsCode, String originCountryCode

    ) {
        this.name = name;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.valueCurrency = valueCurrency;
        this.weight = weight;
        this.weightUnit = weightUnit;
        this.hsCode = hsCode;
        this.originCountryCode = originCountryCode;
    }

    public static OrderItem of(OrderItemDto dto) {
        return OrderItem.builder()
            .name(dto.itemName())
            .quantity(dto.itemQuantity())
            .unitValue(BigDecimal.valueOf(dto.itemUnitValue()))
            .valueCurrency(dto.itemValueCurrency())
            .weight(BigDecimal.valueOf(dto.itemWeight()))
            .weightUnit(dto.weightUnit())
            .hsCode(dto.itemHSCode())
            .originCountryCode(dto.itemOriginCountryCode())
            .build();
    }


    public void updateFrom(OrderItemRequest req) {
        this.name = req.itemName();
        this.quantity = req.itemQuantity();
        this.unitValue = BigDecimal.valueOf(req.itemUnitValue());
        this.valueCurrency = req.itemValueCurrency();
        this.weight = BigDecimal.valueOf(req.itemWeight());
        this.weightUnit = req.weightUnit();
        this.hsCode = req.itemHSCode();
        this.originCountryCode = req.itemOriginCountryCode();
    }

    /**
     * 해당 OrderItem이 소속된 주문(Order)을 설정
     *
     * @param order 이 아이템이 속한 주문 엔티티
     */
    public void assignOrder(Order order) {
        this.order = order;
    }
}