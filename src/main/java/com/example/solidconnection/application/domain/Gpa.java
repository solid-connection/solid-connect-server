package com.example.solidconnection.application.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Embeddable
public class Gpa {

    @Column(nullable = false, name = "gpa")
    private Float gpa;

    @Column(nullable = false, name = "gpa_creteria")
    private Float gpaCriteria;

    @Column(nullable = false, name = "gpa_report_url", length = 500)
    private String gpaReportUrl;

    protected Gpa() {}
}
