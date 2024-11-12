package com.example.solidconnection.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Embeddable
@EqualsAndHashCode(of = {"gpa", "gpaCriteria", "gpaReportUrl"})
public class Gpa {

    @Column(nullable = false, name = "gpa")
    private Double gpa;

    @Column(nullable = false, name = "gpa_criteria")
    private Double gpaCriteria;

    @Column(nullable = false, name = "gpa_report_url", length = 500)
    private String gpaReportUrl;
}
