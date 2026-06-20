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
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(name = "semester_available_for_dispatch")
    @Enumerated(EnumType.STRING)
    private SemesterAvailableForDispatch semesterAvailableForDispatch;

    @Column(name = "semester_requirement", length = 100)
    private String semesterRequirement;

    @Column(name = "details_for_language", length = 2000)
    private String detailsForLanguage;

    @Column(name = "gpa_requirement", length = 100)
    private String gpaRequirement;

    @Column(name = "gpa_requirement_criteria", length = 100)
    private String gpaRequirementCriteria;

    @Column(name = "details_for_accommodation", length = 2000)
    private String detailsForAccommodation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_info", columnDefinition = "JSON")
    private Map<String, String> extraInfo;

    @OneToMany(mappedBy = "univApplyInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LanguageRequirement> languageRequirements = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private HostUniversity university;

    public void addLanguageRequirements(LanguageRequirement languageRequirements) {
        this.languageRequirements.add(languageRequirements);
    }

    public void update(
            Integer studentCapacity,
            SemesterAvailableForDispatch semesterAvailableForDispatch,
            String semesterRequirement,
            String detailsForLanguage,
            String gpaRequirement,
            String gpaRequirementCriteria,
            String detailsForAccommodation,
            Map<String, String> extraInfo
    ) {
        this.studentCapacity = studentCapacity;
        this.semesterAvailableForDispatch = semesterAvailableForDispatch;
        this.semesterRequirement = semesterRequirement;
        this.detailsForLanguage = detailsForLanguage;
        this.gpaRequirement = gpaRequirement;
        this.gpaRequirementCriteria = gpaRequirementCriteria;
        this.detailsForAccommodation = detailsForAccommodation;
        this.extraInfo = extraInfo;
    }

    public void clearLanguageRequirements() {
        this.languageRequirements.clear();
    }

    public void updateExtraInfo(Map<String, String> extraInfo) {
        this.extraInfo = extraInfo;
    }
}
