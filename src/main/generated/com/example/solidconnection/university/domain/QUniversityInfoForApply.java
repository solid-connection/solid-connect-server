package com.example.solidconnection.university.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUniversityInfoForApply is a Querydsl query type for UniversityInfoForApply
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUniversityInfoForApply extends EntityPathBase<UniversityInfoForApply> {

    private static final long serialVersionUID = 31331617L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUniversityInfoForApply universityInfoForApply = new QUniversityInfoForApply("universityInfoForApply");

    public final StringPath details = createString("details");

    public final StringPath detailsForAccommodation = createString("detailsForAccommodation");

    public final StringPath detailsForApply = createString("detailsForApply");

    public final StringPath detailsForEnglishCourse = createString("detailsForEnglishCourse");

    public final StringPath detailsForLanguage = createString("detailsForLanguage");

    public final StringPath detailsForMajor = createString("detailsForMajor");

    public final StringPath gpaRequirement = createString("gpaRequirement");

    public final StringPath gpaRequirementCriteria = createString("gpaRequirementCriteria");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath koreanName = createString("koreanName");

    public final SetPath<LanguageRequirement, QLanguageRequirement> languageRequirements = this.<LanguageRequirement, QLanguageRequirement>createSet("languageRequirements", LanguageRequirement.class, QLanguageRequirement.class, PathInits.DIRECT2);

    public final EnumPath<com.example.solidconnection.type.SemesterAvailableForDispatch> semesterAvailableForDispatch = createEnum("semesterAvailableForDispatch", com.example.solidconnection.type.SemesterAvailableForDispatch.class);

    public final StringPath semesterRequirement = createString("semesterRequirement");

    public final NumberPath<Integer> studentCapacity = createNumber("studentCapacity", Integer.class);

    public final StringPath term = createString("term");

    public final EnumPath<com.example.solidconnection.type.TuitionFeeType> tuitionFeeType = createEnum("tuitionFeeType", com.example.solidconnection.type.TuitionFeeType.class);

    public final QUniversity university;

    public QUniversityInfoForApply(String variable) {
        this(UniversityInfoForApply.class, forVariable(variable), INITS);
    }

    public QUniversityInfoForApply(Path<? extends UniversityInfoForApply> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUniversityInfoForApply(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUniversityInfoForApply(PathMetadata metadata, PathInits inits) {
        this(UniversityInfoForApply.class, metadata, inits);
    }

    public QUniversityInfoForApply(Class<? extends UniversityInfoForApply> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.university = inits.isInitialized("university") ? new QUniversity(forProperty("university"), inits.get("university")) : null;
    }

}

