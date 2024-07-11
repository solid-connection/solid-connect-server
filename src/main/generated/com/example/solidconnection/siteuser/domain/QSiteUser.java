package com.example.solidconnection.siteuser.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSiteUser is a Querydsl query type for SiteUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSiteUser extends EntityPathBase<SiteUser> {

    private static final long serialVersionUID = 1080517302L;

    public static final QSiteUser siteUser = new QSiteUser("siteUser");

    public final StringPath birth = createString("birth");

    public final StringPath email = createString("email");

    public final EnumPath<com.example.solidconnection.type.Gender> gender = createEnum("gender", com.example.solidconnection.type.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nickname = createString("nickname");

    public final DateTimePath<java.time.LocalDateTime> nicknameModifiedAt = createDateTime("nicknameModifiedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.solidconnection.type.PreparationStatus> preparationStage = createEnum("preparationStage", com.example.solidconnection.type.PreparationStatus.class);

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final DatePath<java.time.LocalDate> quitedAt = createDate("quitedAt", java.time.LocalDate.class);

    public final EnumPath<com.example.solidconnection.type.Role> role = createEnum("role", com.example.solidconnection.type.Role.class);

    public QSiteUser(String variable) {
        super(SiteUser.class, forVariable(variable));
    }

    public QSiteUser(Path<? extends SiteUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSiteUser(PathMetadata metadata) {
        super(SiteUser.class, metadata);
    }

}

