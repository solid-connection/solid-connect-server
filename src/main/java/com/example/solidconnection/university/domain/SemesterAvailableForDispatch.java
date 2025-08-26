package com.example.solidconnection.university.domain;

public enum SemesterAvailableForDispatch {
    ONE_SEMESTER("1개학기"),
    TWO_SEMESTER("2개학기"),
    FOUR_SEMESTER("4개학기"),
    ONE_OR_TWO_SEMESTER("1개 또는 2개 학기"),
    ONE_YEAR("1년만 가능"),
    IRRELEVANT("무관"),
    NO_DATA("데이터 없음"),
    ;

    private final String koreanName;

    SemesterAvailableForDispatch(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
