package com.example.solidconnection.university.domain;

import com.example.solidconnection.type.SemesterAvailableForDispatch;
import com.example.solidconnection.type.TuitionFeeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class UniversityInfoForApply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String term;

    @Column
    private Integer studentCapacity;

    @Column
    @Enumerated(EnumType.STRING)
    private TuitionFeeType tuitionFeeType;

    @Column
    @Enumerated(EnumType.STRING)
    private SemesterAvailableForDispatch semesterAvailableForDispatch;

    @Column(length = 100)
    private String semesterRequirement;

    @Column(length = 1000)
    private String detailsForLanguage;

    @Column(length = 100)
    private String gpaRequirement;

    @Column(length = 100)
    private String gpaRequirementCriteria;

    @Column(length = 1000)
    private String detailsForApply;

    @Column(length = 1000)
    private String detailsForMajor;

    @Column(length = 1000)
    private String detailsForAccommodation;

    @Column(length = 1000)
    private String detailsForEnglishCourse;

    @Column(length = 500)
    private String details;

    @OneToMany(mappedBy = "universityInfoForApply", fetch = FetchType.LAZY)
    private Set<LanguageRequirement> languageRequirements;

    @OneToOne
    private University university;
}
