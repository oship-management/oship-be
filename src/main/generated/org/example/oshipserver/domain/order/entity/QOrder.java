package org.example.oshipserver.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrder is a Querydsl query type for Order
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrder extends EntityPathBase<Order> {

    private static final long serialVersionUID = 262630047L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrder order = new QOrder("order1");

    public final org.example.oshipserver.global.entity.QBaseTimeEntity _super = new org.example.oshipserver.global.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> awbPrintedAt = createDateTime("awbPrintedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.OrderStatus> currentStatus = createEnum("currentStatus", org.example.oshipserver.domain.order.entity.enums.OrderStatus.class);

    public final BooleanPath deleted = createBoolean("deleted");

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.DeleterRole> deletedBy = createEnum("deletedBy", org.example.oshipserver.domain.order.entity.enums.DeleterRole.class);

    public final DateTimePath<java.time.LocalDateTime> deliveredAt = createDateTime("deliveredAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> deliveryDays = createNumber("deliveryDays", Integer.class);

    public final NumberPath<java.math.BigDecimal> dimensionHeight = createNumber("dimensionHeight", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> dimensionLength = createNumber("dimensionLength", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> dimensionWidth = createNumber("dimensionWidth", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPrintAwb = createBoolean("isPrintAwb");

    public final BooleanPath isPrintBarcode = createBoolean("isPrintBarcode");

    public final StringPath lastTrackingEvent = createString("lastTrackingEvent");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final DateTimePath<java.time.LocalDateTime> orderedAt = createDateTime("orderedAt", java.time.LocalDateTime.class);

    public final ListPath<OrderItem, QOrderItem> orderItems = this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class, PathInits.DIRECT2);

    public final StringPath orderNo = createString("orderNo");

    public final StringPath oshipMasterNo = createString("oshipMasterNo");

    public final NumberPath<Integer> parcelCount = createNumber("parcelCount", Integer.class);

    public final NumberPath<Long> parterId = createNumber("parterId", Long.class);

    public final QOrderRecipient recipient;

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public final QOrderSender sender;

    public final NumberPath<java.math.BigDecimal> shipmentActualWeight = createNumber("shipmentActualWeight", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> shipmentVolumeWeight = createNumber("shipmentVolumeWeight", java.math.BigDecimal.class);

    public final StringPath weightUnit = createString("weightUnit");

    public QOrder(String variable) {
        this(Order.class, forVariable(variable), INITS);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        this(Order.class, metadata, inits);
    }

    public QOrder(Class<? extends Order> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.recipient = inits.isInitialized("recipient") ? new QOrderRecipient(forProperty("recipient"), inits.get("recipient")) : null;
        this.sender = inits.isInitialized("sender") ? new QOrderSender(forProperty("sender"), inits.get("sender")) : null;
    }

}

