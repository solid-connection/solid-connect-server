package com.example.solidconnection.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterestedCountry is a Querydsl query type for InterestedCountry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterestedCountry extends EntityPathBase<InterestedCountry> {

    private static final long serialVersionUID = 1105130488L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterestedCountry interestedCountry = new QInterestedCountry("interestedCountry");

    public final QCountry country;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.solidconnection.siteuser.domain.QSiteUser siteUser;

    public QInterestedCountry(String variable) {
        this(InterestedCountry.class, forVariable(variable), INITS);
    }

    public QInterestedCountry(Path<? extends InterestedCountry> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterestedCountry(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterestedCountry(PathMetadata metadata, PathInits inits) {
        this(InterestedCountry.class, metadata, inits);
    }

    public QInterestedCountry(Class<? extends InterestedCountry> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new QCountry(forProperty("country"), inits.get("country")) : null;
        this.siteUser = inits.isInitialized("siteUser") ? new com.example.solidconnection.siteuser.domain.QSiteUser(forProperty("siteUser")) : null;
    }

}

