package org.example.oshipserver.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSenderAddress is a Querydsl query type for SenderAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSenderAddress extends EntityPathBase<SenderAddress> {

    private static final long serialVersionUID = -244898224L;

    public static final QSenderAddress senderAddress = new QSenderAddress("senderAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath senderAddress1 = createString("senderAddress1");

    public final StringPath senderAddress2 = createString("senderAddress2");

    public final StringPath senderCity = createString("senderCity");

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.CountryCode> senderCountryCode = createEnum("senderCountryCode", org.example.oshipserver.domain.order.entity.enums.CountryCode.class);

    public final StringPath senderState = createString("senderState");

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.StateCode> senderStateCode = createEnum("senderStateCode", org.example.oshipserver.domain.order.entity.enums.StateCode.class);

    public final StringPath senderTaxId = createString("senderTaxId");

    public final StringPath senderZipCode = createString("senderZipCode");

    public QSenderAddress(String variable) {
        super(SenderAddress.class, forVariable(variable));
    }

    public QSenderAddress(Path<? extends SenderAddress> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSenderAddress(PathMetadata metadata) {
        super(SenderAddress.class, metadata);
    }

}

