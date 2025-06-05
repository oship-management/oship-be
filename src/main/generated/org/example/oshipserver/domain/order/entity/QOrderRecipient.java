package org.example.oshipserver.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrderRecipient is a Querydsl query type for OrderRecipient
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrderRecipient extends EntityPathBase<OrderRecipient> {

    private static final long serialVersionUID = 1728096826L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrderRecipient orderRecipient = new QOrderRecipient("orderRecipient");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOrder order;

    public final QRecipientAddress recipientAddress;

    public final StringPath recipientCompany = createString("recipientCompany");

    public final StringPath recipientEmail = createString("recipientEmail");

    public final StringPath recipientName = createString("recipientName");

    public final StringPath recipientPhoneNo = createString("recipientPhoneNo");

    public QOrderRecipient(String variable) {
        this(OrderRecipient.class, forVariable(variable), INITS);
    }

    public QOrderRecipient(Path<? extends OrderRecipient> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrderRecipient(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrderRecipient(PathMetadata metadata, PathInits inits) {
        this(OrderRecipient.class, metadata, inits);
    }

    public QOrderRecipient(Class<? extends OrderRecipient> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order"), inits.get("order")) : null;
        this.recipientAddress = inits.isInitialized("recipientAddress") ? new QRecipientAddress(forProperty("recipientAddress")) : null;
    }

}

