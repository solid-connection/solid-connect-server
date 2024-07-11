package com.example.solidconnection.application.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGpa is a Querydsl query type for Gpa
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QGpa extends BeanPath<Gpa> {

    private static final long serialVersionUID = -51081086L;

    public static final QGpa gpa1 = new QGpa("gpa1");

    public final NumberPath<Double> gpa = createNumber("gpa", Double.class);

    public final NumberPath<Double> gpaCriteria = createNumber("gpaCriteria", Double.class);

    public final StringPath gpaReportUrl = createString("gpaReportUrl");

    public QGpa(String variable) {
        super(Gpa.class, forVariable(variable));
    }

    public QGpa(Path<? extends Gpa> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGpa(PathMetadata metadata) {
        super(Gpa.class, metadata);
    }

}

