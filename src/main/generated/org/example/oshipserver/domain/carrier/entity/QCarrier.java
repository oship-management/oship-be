package org.example.oshipserver.domain.carrier.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCarrier is a Querydsl query type for Carrier
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCarrier extends EntityPathBase<Carrier> {

    private static final long serialVersionUID = -249898465L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCarrier carrier = new QCarrier("carrier");

    public final org.example.oshipserver.global.entity.QBaseTimeEntity _super = new org.example.oshipserver.global.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final EnumPath<org.example.oshipserver.domain.carrier.enums.CarrierName> name = createEnum("name", org.example.oshipserver.domain.carrier.enums.CarrierName.class);

    public final org.example.oshipserver.domain.partner.entity.QPartner partner;

    public final EnumPath<org.example.oshipserver.domain.carrier.enums.Services> service = createEnum("service", org.example.oshipserver.domain.carrier.enums.Services.class);

    public final StringPath token = createString("token");

    public final NumberPath<java.math.BigDecimal> weightMax = createNumber("weightMax", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> weightMin = createNumber("weightMin", java.math.BigDecimal.class);

    public QCarrier(String variable) {
        this(Carrier.class, forVariable(variable), INITS);
    }

    public QCarrier(Path<? extends Carrier> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCarrier(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCarrier(PathMetadata metadata, PathInits inits) {
        this(Carrier.class, metadata, inits);
    }

    public QCarrier(Class<? extends Carrier> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.partner = inits.isInitialized("partner") ? new org.example.oshipserver.domain.partner.entity.QPartner(forProperty("partner")) : null;
    }

}

