package org.example.oshipserver.domain.carrier.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarrierCountry is a Querydsl query type for CarrierCountry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarrierCountry extends EntityPathBase<CarrierCountry> {

    private static final long serialVersionUID = 1852851319L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarrierCountry carrierCountry = new QCarrierCountry("carrierCountry");

    public final org.example.oshipserver.global.entity.QBaseTimeEntity _super = new org.example.oshipserver.global.entity.QBaseTimeEntity(this);

    public final QCarrier carrier;

    public final StringPath countryCode = createString("countryCode");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<Integer> zoneNo = createNumber("zoneNo", Integer.class);

    public QCarrierCountry(String variable) {
        this(CarrierCountry.class, forVariable(variable), INITS);
    }

    public QCarrierCountry(Path<? extends CarrierCountry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarrierCountry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarrierCountry(PathMetadata metadata, PathInits inits) {
        this(CarrierCountry.class, metadata, inits);
    }

    public QCarrierCountry(Class<? extends CarrierCountry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.carrier = inits.isInitialized("carrier") ? new QCarrier(forProperty("carrier"), inits.get("carrier")) : null;
    }

}

