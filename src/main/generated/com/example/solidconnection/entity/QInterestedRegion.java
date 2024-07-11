package com.example.solidconnection.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterestedRegion is a Querydsl query type for InterestedRegion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterestedRegion extends EntityPathBase<InterestedRegion> {

    private static final long serialVersionUID = -1345685934L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterestedRegion interestedRegion = new QInterestedRegion("interestedRegion");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QRegion region;

    public final com.example.solidconnection.siteuser.domain.QSiteUser siteUser;

    public QInterestedRegion(String variable) {
        this(InterestedRegion.class, forVariable(variable), INITS);
    }

    public QInterestedRegion(Path<? extends InterestedRegion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterestedRegion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterestedRegion(PathMetadata metadata, PathInits inits) {
        this(InterestedRegion.class, metadata, inits);
    }

    public QInterestedRegion(Class<? extends InterestedRegion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.region = inits.isInitialized("region") ? new QRegion(forProperty("region")) : null;
        this.siteUser = inits.isInitialized("siteUser") ? new com.example.solidconnection.siteuser.domain.QSiteUser(forProperty("siteUser")) : null;
    }

}

