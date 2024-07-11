package com.example.solidconnection.application.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApplication is a Querydsl query type for Application
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApplication extends EntityPathBase<Application> {

    private static final long serialVersionUID = -122324166L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApplication application = new QApplication("application");

    public final com.example.solidconnection.university.domain.QUniversityInfoForApply firstChoiceUniversity;

    public final QGpa gpa;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLanguageTest languageTest;

    public final StringPath nicknameForApply = createString("nicknameForApply");

    public final com.example.solidconnection.university.domain.QUniversityInfoForApply secondChoiceUniversity;

    public final com.example.solidconnection.siteuser.domain.QSiteUser siteUser;

    public final NumberPath<Integer> updateCount = createNumber("updateCount", Integer.class);

    public final EnumPath<com.example.solidconnection.type.VerifyStatus> verifyStatus = createEnum("verifyStatus", com.example.solidconnection.type.VerifyStatus.class);

    public QApplication(String variable) {
        this(Application.class, forVariable(variable), INITS);
    }

    public QApplication(Path<? extends Application> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApplication(PathMetadata metadata, PathInits inits) {
        this(Application.class, metadata, inits);
    }

    public QApplication(Class<? extends Application> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.firstChoiceUniversity = inits.isInitialized("firstChoiceUniversity") ? new com.example.solidconnection.university.domain.QUniversityInfoForApply(forProperty("firstChoiceUniversity"), inits.get("firstChoiceUniversity")) : null;
        this.gpa = inits.isInitialized("gpa") ? new QGpa(forProperty("gpa")) : null;
        this.languageTest = inits.isInitialized("languageTest") ? new QLanguageTest(forProperty("languageTest")) : null;
        this.secondChoiceUniversity = inits.isInitialized("secondChoiceUniversity") ? new com.example.solidconnection.university.domain.QUniversityInfoForApply(forProperty("secondChoiceUniversity"), inits.get("secondChoiceUniversity")) : null;
        this.siteUser = inits.isInitialized("siteUser") ? new com.example.solidconnection.siteuser.domain.QSiteUser(forProperty("siteUser")) : null;
    }

}

