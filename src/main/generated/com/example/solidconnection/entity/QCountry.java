package com.example.solidconnection.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCountry is a Querydsl query type for Country
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCountry extends EntityPathBase<Country> {

    private static final long serialVersionUID = -2001953983L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCountry country = new QCountry("country");

    public final StringPath code = createString("code");

    public final StringPath koreanName = createString("koreanName");

    public final QRegion region;

    public QCountry(String variable) {
        this(Country.class, forVariable(variable), INITS);
    }

    public QCountry(Path<? extends Country> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCountry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCountry(PathMetadata metadata, PathInits inits) {
        this(Country.class, metadata, inits);
    }

    public QCountry(Class<? extends Country> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.region = inits.isInitialized("region") ? new QRegion(forProperty("region")) : null;
    }

}

