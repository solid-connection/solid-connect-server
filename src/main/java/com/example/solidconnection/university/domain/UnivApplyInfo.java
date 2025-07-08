package com.example.solidconnection.university.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "university_info_for_apply")
public class UnivApplyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String term;

    @Column(nullable = false, length = 100)
    private String koreanName;

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

    @Column(length = 1000)
    private String details;

    @OneToMany(mappedBy = "univApplyInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LanguageRequirement> languageRequirements = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private University university;

    public void addLanguageRequirements(LanguageRequirement languageRequirements) {
        this.languageRequirements.add(languageRequirements);
    }
}
