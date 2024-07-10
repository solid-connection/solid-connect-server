package com.example.solidconnection.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Embeddable
public class Gpa {

    @Column(nullable = false, name = "gpa")
    private Double gpa;

    @Column(nullable = false, name = "gpa_creteria")
    private Double gpaCriteria;

    @Column(nullable = false, name = "gpa_report_url", length = 500)
    private String gpaReportUrl;
}
