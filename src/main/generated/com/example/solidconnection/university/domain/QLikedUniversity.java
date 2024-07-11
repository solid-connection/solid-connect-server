package com.example.solidconnection.university.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLikedUniversity is a Querydsl query type for LikedUniversity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLikedUniversity extends EntityPathBase<LikedUniversity> {

    private static final long serialVersionUID = 142590363L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLikedUniversity likedUniversity = new QLikedUniversity("likedUniversity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.solidconnection.siteuser.domain.QSiteUser siteUser;

    public final QUniversityInfoForApply universityInfoForApply;

    public QLikedUniversity(String variable) {
        this(LikedUniversity.class, forVariable(variable), INITS);
    }

    public QLikedUniversity(Path<? extends LikedUniversity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLikedUniversity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLikedUniversity(PathMetadata metadata, PathInits inits) {
        this(LikedUniversity.class, metadata, inits);
    }

    public QLikedUniversity(Class<? extends LikedUniversity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.siteUser = inits.isInitialized("siteUser") ? new com.example.solidconnection.siteuser.domain.QSiteUser(forProperty("siteUser")) : null;
        this.universityInfoForApply = inits.isInitialized("universityInfoForApply") ? new QUniversityInfoForApply(forProperty("universityInfoForApply"), inits.get("universityInfoForApply")) : null;
    }

}

