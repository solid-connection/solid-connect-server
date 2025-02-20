package com.example.solidconnection.siteuser.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSiteUser is a Querydsl query type for SiteUser
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSiteUser extends EntityPathBase<SiteUser> {

    private static final long serialVersionUID = 1080517302L;

    public static final QSiteUser siteUser = new QSiteUser("siteUser");

    public final EnumPath<AuthType> authType = createEnum("authType", AuthType.class);

    public final StringPath birth = createString("birth");

    public final ListPath<com.example.solidconnection.community.comment.domain.Comment, com.example.solidconnection.community.comment.domain.QComment> commentList = this.<com.example.solidconnection.community.comment.domain.Comment, com.example.solidconnection.community.comment.domain.QComment>createList("commentList", com.example.solidconnection.community.comment.domain.Comment.class, com.example.solidconnection.community.comment.domain.QComment.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final EnumPath<com.example.solidconnection.type.Gender> gender = createEnum("gender", com.example.solidconnection.type.Gender.class);

    public final ListPath<com.example.solidconnection.score.domain.GpaScore, com.example.solidconnection.score.domain.QGpaScore> gpaScoreList = this.<com.example.solidconnection.score.domain.GpaScore, com.example.solidconnection.score.domain.QGpaScore>createList("gpaScoreList", com.example.solidconnection.score.domain.GpaScore.class, com.example.solidconnection.score.domain.QGpaScore.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<com.example.solidconnection.score.domain.LanguageTestScore, com.example.solidconnection.score.domain.QLanguageTestScore> languageTestScoreList = this.<com.example.solidconnection.score.domain.LanguageTestScore, com.example.solidconnection.score.domain.QLanguageTestScore>createList("languageTestScoreList", com.example.solidconnection.score.domain.LanguageTestScore.class, com.example.solidconnection.score.domain.QLanguageTestScore.class, PathInits.DIRECT2);

    public final StringPath nickname = createString("nickname");

    public final DateTimePath<java.time.LocalDateTime> nicknameModifiedAt = createDateTime("nicknameModifiedAt", java.time.LocalDateTime.class);

    public final StringPath password = createString("password");

    public final ListPath<com.example.solidconnection.community.post.domain.PostLike, com.example.solidconnection.community.post.domain.QPostLike> postLikeList = this.<com.example.solidconnection.community.post.domain.PostLike, com.example.solidconnection.community.post.domain.QPostLike>createList("postLikeList", com.example.solidconnection.community.post.domain.PostLike.class, com.example.solidconnection.community.post.domain.QPostLike.class, PathInits.DIRECT2);

    public final ListPath<com.example.solidconnection.community.post.domain.Post, com.example.solidconnection.community.post.domain.QPost> postList = this.<com.example.solidconnection.community.post.domain.Post, com.example.solidconnection.community.post.domain.QPost>createList("postList", com.example.solidconnection.community.post.domain.Post.class, com.example.solidconnection.community.post.domain.QPost.class, PathInits.DIRECT2);

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

