package org.example.oshipserver.domain.auth.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuthAddress is a Querydsl query type for AuthAddress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuthAddress extends EntityPathBase<AuthAddress> {

    private static final long serialVersionUID = 629435499L;

    public static final QAuthAddress authAddress = new QAuthAddress("authAddress");

    public final StringPath city = createString("city");

    public final StringPath country = createString("country");

    public final StringPath detail1 = createString("detail1");

    public final StringPath detail2 = createString("detail2");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath state = createString("state");

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final StringPath zipCode = createString("zipCode");

    public QAuthAddress(String variable) {
        super(AuthAddress.class, forVariable(variable));
    }

    public QAuthAddress(Path<? extends AuthAddress> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthAddress(PathMetadata metadata) {
        super(AuthAddress.class, metadata);
    }

}

