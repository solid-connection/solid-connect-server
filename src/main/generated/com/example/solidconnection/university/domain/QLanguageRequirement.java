package com.example.solidconnection.university.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLanguageRequirement is a Querydsl query type for LanguageRequirement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLanguageRequirement extends EntityPathBase<LanguageRequirement> {

    private static final long serialVersionUID = 443667787L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLanguageRequirement languageRequirement = new QLanguageRequirement("languageRequirement");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.example.solidconnection.type.LanguageTestType> languageTestType = createEnum("languageTestType", com.example.solidconnection.type.LanguageTestType.class);

    public final StringPath minScore = createString("minScore");

    public final QUniversityInfoForApply universityInfoForApply;

    public QLanguageRequirement(String variable) {
        this(LanguageRequirement.class, forVariable(variable), INITS);
    }

    public QLanguageRequirement(Path<? extends LanguageRequirement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLanguageRequirement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLanguageRequirement(PathMetadata metadata, PathInits inits) {
        this(LanguageRequirement.class, metadata, inits);
    }

    public QLanguageRequirement(Class<? extends LanguageRequirement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.universityInfoForApply = inits.isInitialized("universityInfoForApply") ? new QUniversityInfoForApply(forProperty("universityInfoForApply"), inits.get("universityInfoForApply")) : null;
    }

}

