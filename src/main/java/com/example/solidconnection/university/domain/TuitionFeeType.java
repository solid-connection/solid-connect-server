package com.example.solidconnection.university.domain;

public enum TuitionFeeType {
    HOME_UNIVERSITY_PAYMENT("본교등록금납부형"),
    OVERSEAS_UNIVERSITY_PAYMENT("해외대학등록금납부형"),
    MIXED_PAYMENT("혼합형");

    private final String koreanName;

    TuitionFeeType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }
}
