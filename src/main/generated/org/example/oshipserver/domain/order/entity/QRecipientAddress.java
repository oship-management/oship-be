package org.example.oshipserver.domain.order.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRecipientAddress is a Querydsl query type for RecipientAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecipientAddress extends EntityPathBase<RecipientAddress> {

    private static final long serialVersionUID = -30706774L;

    public static final QRecipientAddress recipientAddress = new QRecipientAddress("recipientAddress");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath recipientAddress1 = createString("recipientAddress1");

    public final StringPath recipientAddress2 = createString("recipientAddress2");

    public final StringPath recipientCity = createString("recipientCity");

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.CountryCode> recipientCountryCode = createEnum("recipientCountryCode", org.example.oshipserver.domain.order.entity.enums.CountryCode.class);

    public final StringPath recipientState = createString("recipientState");

    public final EnumPath<org.example.oshipserver.domain.order.entity.enums.StateCode> recipientStateCode = createEnum("recipientStateCode", org.example.oshipserver.domain.order.entity.enums.StateCode.class);

    public final StringPath recipientTaxId = createString("recipientTaxId");

    public final StringPath recipientZipCode = createString("recipientZipCode");

    public QRecipientAddress(String variable) {
        super(RecipientAddress.class, forVariable(variable));
    }

    public QRecipientAddress(Path<? extends RecipientAddress> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRecipientAddress(PathMetadata metadata) {
        super(RecipientAddress.class, metadata);
    }

}

