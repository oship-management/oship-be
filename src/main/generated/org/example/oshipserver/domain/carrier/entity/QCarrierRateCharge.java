package org.example.oshipserver.domain.carrier.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarrierRateCharge is a Querydsl query type for CarrierRateCharge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarrierRateCharge extends EntityPathBase<CarrierRateCharge> {

    private static final long serialVersionUID = 1997672243L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarrierRateCharge carrierRateCharge = new QCarrierRateCharge("carrierRateCharge");

    public final org.example.oshipserver.global.entity.QBaseTimeEntity _super = new org.example.oshipserver.global.entity.QBaseTimeEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final QCarrier carrier;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final NumberPath<java.math.BigDecimal> weight = createNumber("weight", java.math.BigDecimal.class);

    public final NumberPath<Integer> zoneIndex = createNumber("zoneIndex", Integer.class);

    public QCarrierRateCharge(String variable) {
        this(CarrierRateCharge.class, forVariable(variable), INITS);
    }

    public QCarrierRateCharge(Path<? extends CarrierRateCharge> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarrierRateCharge(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarrierRateCharge(PathMetadata metadata, PathInits inits) {
        this(CarrierRateCharge.class, metadata, inits);
    }

    public QCarrierRateCharge(Class<? extends CarrierRateCharge> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.carrier = inits.isInitialized("carrier") ? new QCarrier(forProperty("carrier"), inits.get("carrier")) : null;
    }

}

