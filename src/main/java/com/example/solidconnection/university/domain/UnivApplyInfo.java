package com.example.solidconnection.university.domain;

import com.example.solidconnection.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "university_info_for_apply")
public class UnivApplyInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "term_id", nullable = false)
    private long termId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_university_id")
    private HomeUniversity homeUniversity;

    @Column(name = "korean_name", nullable = false, length = 100)
    private String koreanName;

    @Column(name = "student_capacity")
    private Integer studentCapacity;

    @Column(name = "tuition_fee_type")
    @Enumerated(EnumType.STRING)
    private TuitionFeeType tuitionFeeType;

    @Column(name = "semester_available_for_dispatch")
    @Enumerated(EnumType.STRING)
    private SemesterAvailableForDispatch semesterAvailableForDispatch;

    @Column(name = "semester_requirement", length = 100)
    private String semesterRequirement;

    @Column(name = "details_for_language", length = 1000)
    private String detailsForLanguage;

    @Column(name = "gpa_requirement", length = 100)
    private String gpaRequirement;

    @Column(name = "gpa_requirement_criteria", length = 100)
    private String gpaRequirementCriteria;

    @Column(name = "details_for_apply", length = 1000)
    private String detailsForApply;

    @Column(name = "details_for_major", length = 1000)
    private String detailsForMajor;

    @Column(name = "details_for_accommodation", length = 1000)
    private String detailsForAccommodation;

    @Column(name = "details_for_english_course", length = 1000)
    private String detailsForEnglishCourse;

    @Column(name = "details", length = 1000)
    private String details;

    @OneToMany(mappedBy = "univApplyInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LanguageRequirement> languageRequirements = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HostUniversity university;

    public void addLanguageRequirements(LanguageRequirement languageRequirements) {
        this.languageRequirements.add(languageRequirements);
    }
}
