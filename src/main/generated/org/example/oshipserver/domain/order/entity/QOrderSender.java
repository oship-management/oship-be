package org.example.oshipserver.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderSender is a Querydsl query type for OrderSender
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderSender extends EntityPathBase<OrderSender> {

    private static final long serialVersionUID = 961789364L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderSender orderSender = new QOrderSender("orderSender");

    public final QSenderAddress address;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrder order;

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public final StringPath senderEmail = createString("senderEmail");

    public final StringPath senderName = createString("senderName");

    public final StringPath senderPhoneNo = createString("senderPhoneNo");

    public final StringPath storeName = createString("storeName");

    public final StringPath storePlatform = createString("storePlatform");

    public QOrderSender(String variable) {
        this(OrderSender.class, forVariable(variable), INITS);
    }

    public QOrderSender(Path<? extends OrderSender> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderSender(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderSender(PathMetadata metadata, PathInits inits) {
        this(OrderSender.class, metadata, inits);
    }

    public QOrderSender(Class<? extends OrderSender> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new QSenderAddress(forProperty("address")) : null;
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

