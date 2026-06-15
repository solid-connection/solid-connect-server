package com.example.solidconnection.university.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnivApplyInfoColumn {

    UNIVERSITY_KOREAN_NAME("universityKoreanName", "대학명", "학교명", "대학교명"),
    STUDENT_CAPACITY("studentCapacity", "인원", "모집인원", "정원", "모집정원"),
    TUITION_FEE_TYPE("tuitionFeeType", "등록금유형", "수업료유형"),
    SEMESTER_AVAILABLE_FOR_DISPATCH("semesterAvailableForDispatch", "파견가능학기", "파견학기"),
    SEMESTER_REQUIREMENT("semesterRequirement", "학기요건", "재학학기"),
    DETAILS_FOR_LANGUAGE("detailsForLanguage", "어학사항", "어학요건상세"),
    GPA_REQUIREMENT("gpaRequirement", "성적요건", "학점요건", "최소학점"),
    GPA_REQUIREMENT_CRITERIA("gpaRequirementCriteria", "학점기준", "성적기준"),
    DETAILS_FOR_APPLY("detailsForApply", "지원사항", "지원안내"),
    DETAILS_FOR_MAJOR("detailsForMajor", "전공사항", "전공안내"),
    DETAILS_FOR_ACCOMMODATION("detailsForAccommodation", "숙소사항", "기숙사안내"),
    DETAILS_FOR_ENGLISH_COURSE("detailsForEnglishCourse", "영어강좌", "영어강의"),
    DETAILS("details", "기타사항", "비고"),
    ;

    private final String fieldName;
    private final List<String> aliases;

    UnivApplyInfoColumn(String fieldName, String... aliases) {
        this.fieldName = fieldName;
        this.aliases = List.of(aliases);
    }

    public static Optional<UnivApplyInfoColumn> findByAlias(String header) {
        return Arrays.stream(values())
                .filter(col -> col.aliases.contains(header))
                .findFirst();
    }
}
