package com.example.solidconnection.application.domain;

import com.example.solidconnection.type.LanguageTestType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Embeddable
public class LanguageTest {

    @Column(nullable = false, name = "language_test_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LanguageTestType languageTestType;

    @Column(nullable = false, name = "language_test_score")
    private String languageTestScore;

    @Column(nullable = false, name = "language_test_report_url", length = 500)
    private String languageTestReportUrl;

    protected LanguageTest() {}
}
