package com.example.solidconnection.university.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUniversity is a Querydsl query type for University
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUniversity extends EntityPathBase<University> {

    private static final long serialVersionUID = 1195314958L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUniversity university = new QUniversity("university");

    public final StringPath accommodationUrl = createString("accommodationUrl");

    public final StringPath backgroundImageUrl = createString("backgroundImageUrl");

    public final com.example.solidconnection.entity.QCountry country;

    public final StringPath detailsForLocal = createString("detailsForLocal");

    public final StringPath englishCourseUrl = createString("englishCourseUrl");

    public final StringPath englishName = createString("englishName");

    public final StringPath formatName = createString("formatName");

    public final StringPath homepageUrl = createString("homepageUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath koreanName = createString("koreanName");

    public final StringPath logoImageUrl = createString("logoImageUrl");

    public final com.example.solidconnection.entity.QRegion region;

    public QUniversity(String variable) {
        this(University.class, forVariable(variable), INITS);
    }

    public QUniversity(Path<? extends University> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUniversity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUniversity(PathMetadata metadata, PathInits inits) {
        this(University.class, metadata, inits);
    }

    public QUniversity(Class<? extends University> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.country = inits.isInitialized("country") ? new com.example.solidconnection.entity.QCountry(forProperty("country"), inits.get("country")) : null;
        this.region = inits.isInitialized("region") ? new com.example.solidconnection.entity.QRegion(forProperty("region")) : null;
    }

}

