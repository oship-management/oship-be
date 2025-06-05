package org.example.oshipserver.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentOrder is a Querydsl query type for PaymentOrder
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentOrder extends EntityPathBase<PaymentOrder> {

    private static final long serialVersionUID = -1689788881L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentOrder paymentOrder = new QPaymentOrder("paymentOrder");

    public final org.example.oshipserver.global.entity.QBaseTimeEntity _super = new org.example.oshipserver.global.entity.QBaseTimeEntity(this);

    public final DateTimePath<java.time.LocalDateTime> canceledAt = createDateTime("canceledAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> confirmedAt = createDateTime("confirmedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final org.example.oshipserver.domain.order.entity.QOrder order;

    public final QPayment payment;

    public final NumberPath<Integer> paymentAmount = createNumber("paymentAmount", Integer.class);

    public final EnumPath<PaymentStatus> paymentStatus = createEnum("paymentStatus", PaymentStatus.class);

    public QPaymentOrder(String variable) {
        this(PaymentOrder.class, forVariable(variable), INITS);
    }

    public QPaymentOrder(Path<? extends PaymentOrder> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentOrder(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentOrder(PathMetadata metadata, PathInits inits) {
        this(PaymentOrder.class, metadata, inits);
    }

    public QPaymentOrder(Class<? extends PaymentOrder> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new org.example.oshipserver.domain.order.entity.QOrder(forProperty("order"), inits.get("order")) : null;
        this.payment = inits.isInitialized("payment") ? new QPayment(forProperty("payment")) : null;
    }

}

