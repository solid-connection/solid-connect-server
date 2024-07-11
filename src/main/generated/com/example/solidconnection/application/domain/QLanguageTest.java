package com.example.solidconnection.application.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLanguageTest is a Querydsl query type for LanguageTest
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QLanguageTest extends BeanPath<LanguageTest> {

    private static final long serialVersionUID = 1768826720L;

    public static final QLanguageTest languageTest = new QLanguageTest("languageTest");

    public final StringPath languageTestReportUrl = createString("languageTestReportUrl");

    public final StringPath languageTestScore = createString("languageTestScore");

    public final EnumPath<com.example.solidconnection.type.LanguageTestType> languageTestType = createEnum("languageTestType", com.example.solidconnection.type.LanguageTestType.class);

    public QLanguageTest(String variable) {
        super(LanguageTest.class, forVariable(variable));
    }

    public QLanguageTest(Path<? extends LanguageTest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLanguageTest(PathMetadata metadata) {
        super(LanguageTest.class, metadata);
    }

}

